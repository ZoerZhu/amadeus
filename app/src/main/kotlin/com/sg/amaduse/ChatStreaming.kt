package com.sg.amaduse

import android.content.Context
import com.sg.amaduse.agent.audio.AssistantSpeechConfig
import com.sg.amaduse.agent.audio.SpeechOutputCoordinator
import com.sg.amaduse.agent.audio.SpeechTranslationConfig
import com.sg.amaduse.agent.audio.VoiceSettings
import com.sg.amaduse.agent.model.ChatCompletionMode
import com.sg.amaduse.agent.model.ChatCompletionModeAdapter
import com.sg.amaduse.agent.model.ChatCompletionModeConfig
import com.sg.amaduse.agent.persona.PersonaPreset
import com.sg.amaduse.agent.persona.PersonaPromptBuilder
import com.sg.amaduse.agent.persona.PromptMode
import com.sg.amaduse.agent.persona.PromptRuntimeContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal suspend fun streamAssistantMessage(
    context: Context,
    recordId: String,
    messages: MutableList<ChatMessage>,
    assistantIndex: Int,
    userText: String,
    persona: PersonaPreset,
    mode: ChatMode,
    settings: ModelSettings,
    voiceSettings: VoiceSettings,
) {
    fun update(transform: (ChatMessage) -> ChatMessage) {
        if (assistantIndex in messages.indices) {
            messages[assistantIndex] = transform(messages[assistantIndex])
        }
    }

    val requestHistory = messages.take(assistantIndex)
    val speechOutput = SpeechOutputCoordinator.create(
        context = context,
        voiceSettings = voiceSettings,
        translationConfig = settings.toSpeechTranslationConfig(),
        speechConfig = AssistantSpeechConfig(
            voiceOutputLanguageCode = persona.voiceOutputLanguage.code,
            fallbackVoiceUri = persona.ttsVoiceId,
        ),
    )

    val error = fetchOpenAiCompatibleLiveStream(
        settings = settings,
        persona = persona,
        mode = mode,
        history = requestHistory,
        onThinking = { chunk ->
            update { message ->
                message.copy(
                    thinking = message.thinking + chunk,
                    showThinking = true,
                    thinkingExpanded = message.thinking.isBlank() || message.thinkingExpanded,
                )
            }
        },
        onContent = { chunk ->
            speechOutput?.onContent(chunk) { textChunk ->
                update { it.copy(text = it.text + textChunk) }
            } ?: update { it.copy(text = it.text + chunk) }
        },
    )

    if (error != null) {
        splitForStreaming("请求失败：$error").forEach { chunk ->
            update { it.copy(text = it.text + chunk) }
            delay(18)
        }
    }

    if (error == null) {
        speechOutput?.finish { textChunk ->
            update { it.copy(text = it.text + textChunk) }
        }
    } else {
        speechOutput?.cancel()
    }

    update { message ->
        message.copy(
            streaming = false,
            thinkingExpanded = false,
            showThinking = message.thinking.isNotBlank(),
        )
    }
    saveChatMessages(context, recordId, messages)
}

private suspend fun fetchOpenAiCompatibleLiveStream(
    settings: ModelSettings,
    persona: PersonaPreset,
    mode: ChatMode,
    history: List<ChatMessage>,
    onThinking: suspend (String) -> Unit,
    onContent: suspend (String) -> Unit,
): String? = withContext(Dispatchers.IO) {
    if (!settings.useRemote) {
        return@withContext "请先在设置中启用远程 API，或选择 Ollama 等本地 OpenAI-compatible 服务。"
    }
    if (!settings.provider.compatible) {
        return@withContext "${settings.provider.name} 当前只提供配置界面，专用协议尚未接入。"
    }
    if (settings.apiKey.isBlank() && !settings.provider.name.equals("Ollama", ignoreCase = true)) {
        return@withContext "远程 API 需要先在设置中填写 API Key。"
    }

    val base = settings.baseUrl.trim().trimEnd('/')
    val endpoint = "$base/chat/completions"
    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 20_000
        readTimeout = 60_000
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("Accept", "text/event-stream")
        if (settings.apiKey.isNotBlank()) {
            setRequestProperty("Authorization", "Bearer ${settings.apiKey}")
        }
    }

    try {
        val payloadMessages = JSONArray()
            .put(JSONObject().put("role", "system").put("content", buildSystemPrompt(persona, mode)))
        history
            .filter { it.text.isNotBlank() }
            .takeLast(12)
            .forEach { message ->
                payloadMessages.put(
                    JSONObject()
                        .put("role", if (message.isAgent) "assistant" else "user")
                        .put("content", message.text),
                )
            }

        val completionMode = mode.toChatCompletionMode()
        val payload = ChatCompletionModeAdapter.buildPayload(
            config = settings.toChatCompletionModeConfig(),
            mode = completionMode,
            payloadMessages = payloadMessages,
        )

        connection.outputStream.use { output ->
            output.write(payload.toString().toByteArray(Charsets.UTF_8))
        }

        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            return@withContext "HTTP $responseCode ${errorText.take(220)}"
        }

        connection.inputStream.bufferedReader().useLines { lines ->
            lines.forEach { rawLine ->
                val line = rawLine.trim()
                if (!line.startsWith("data:")) {
                    return@forEach
                }
                val data = line.removePrefix("data:").trim()
                if (data == "[DONE]") {
                    return@forEach
                }
                runCatching {
                    val json = JSONObject(data)
                    val choice = json.optJSONArray("choices")?.optJSONObject(0)
                    val delta = choice?.optJSONObject("delta")
                    val content = cleanDisplayText(cleanStreamChunk(delta.cleanString("content")))
                    val reasoning = if (completionMode.shouldReadReasoning) {
                        cleanDisplayText(cleanStreamChunk(delta.cleanString("reasoning_content")))
                            .ifBlank { cleanDisplayText(cleanStreamChunk(delta.cleanString("reasoning"))) }
                    } else {
                        ""
                    }
                    if (completionMode.shouldReadReasoning && reasoning.isNotBlank()) {
                        withContext(Dispatchers.Main) { onThinking(reasoning) }
                    }
                    if (content.isNotBlank()) {
                        withContext(Dispatchers.Main) { onContent(content) }
                    }
                }
            }
        }
        null
    } catch (error: Exception) {
        error.message ?: error.javaClass.simpleName
    } finally {
        connection.disconnect()
    }
}

private fun buildSystemPrompt(
    persona: PersonaPreset,
    mode: ChatMode,
): String {
    return PersonaPromptBuilder.build(
        persona = persona,
        context = PromptRuntimeContext(
            userName = "用户",
            currentTimeText = currentPromptTimeText(),
            mode = mode.toPromptMode(),
        ),
    )
}

private fun ChatMode.toPromptMode(): PromptMode {
    return when (this) {
        ChatMode.Fast -> PromptMode.Fast
        ChatMode.Thinking -> PromptMode.Thinking
    }
}

private fun ChatMode.toChatCompletionMode(): ChatCompletionMode {
    return when (this) {
        ChatMode.Fast -> ChatCompletionMode.Fast
        ChatMode.Thinking -> ChatCompletionMode.Thinking
    }
}

internal fun ModelSettings.toChatCompletionModeConfig(): ChatCompletionModeConfig {
    return ChatCompletionModeConfig(
        providerName = provider.name,
        providerBaseUrl = provider.baseUrl,
        baseUrl = baseUrl,
        model = model,
        defaultModel = provider.defaultModel,
    )
}

private fun ModelSettings.toSpeechTranslationConfig(): SpeechTranslationConfig {
    return SpeechTranslationConfig(
        useRemote = useRemote,
        providerCompatible = provider.compatible,
        providerName = provider.name,
        baseUrl = baseUrl,
        apiKey = apiKey,
        completionConfig = toChatCompletionModeConfig(),
    )
}

private fun currentPromptTimeText(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    }.format(Date())
}
