package com.sg.amaduse

import android.content.Context
import android.webkit.WebView
import com.sg.amaduse.agent.AgentMessage
import com.sg.amaduse.agent.audio.AssistantSpeechConfig
import com.sg.amaduse.agent.runAgentLoop
import com.sg.amaduse.agent.tools.AgentToolContext
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
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
    live2dWebView: WebView? = null,
    agentToolContext: AgentToolContext? = null,
) {
    fun update(transform: (ChatMessage) -> ChatMessage) {
        if (assistantIndex in messages.indices) {
            messages[assistantIndex] = transform(messages[assistantIndex])
        }
    }

    val requestHistory = messages.take(assistantIndex)
    var emittedDisplayBuffer = ""

    suspend fun emitAssistantText(chunk: String) {
        if (chunk.isEmpty()) {
            return
        }
        withContext(Dispatchers.Main) {
            emittedDisplayBuffer += chunk
            update { it.copy(text = emittedDisplayBuffer, activeToolName = null) }
        }
    }

    val speechOutput = SpeechOutputCoordinator.create(
        context = context,
        voiceSettings = voiceSettings,
        translationConfig = settings.toSpeechTranslationConfig(),
        speechConfig = AssistantSpeechConfig(
            voiceOutputLanguageCode = persona.voiceOutputLanguage.code,
            fallbackVoiceUri = persona.ttsVoiceId,
        ),
        emitText = ::emitAssistantText,
    )

    val emotionRegex = Regex("\\[emotion:(\\w+)]")
    val validEmotions = setOf("neutral", "anger", "joy", "sadness", "shy", "smile", "surprise", "unhappy")
    var emotionFired = false
    var rawBuffer = ""
    var displayedBuffer = ""

    suspend fun handleContentChunk(chunk: String) {
        val safeChunk = cleanDisplayText(cleanStreamChunk(chunk))
        if (safeChunk.isEmpty()) {
            return
        }
        rawBuffer += safeChunk
        if (!emotionFired) {
            val match = emotionRegex.find(rawBuffer)
            if (match != null) {
                val emotion = match.groupValues[1]
                if (emotion in validEmotions) {
                    live2dWebView?.evaluateJavascript("playEmotion('$emotion')", null)
                }
                emotionFired = true
            }
        }

        val nextDisplayText = rawBuffer.stripEmotionTagsForDisplay(emotionRegex)
        val cleanChunk = nextDisplayText.removePrefix(displayedBuffer)
        displayedBuffer = nextDisplayText
        if (cleanChunk.isEmpty()) {
            if (speechOutput == null) {
                emittedDisplayBuffer = nextDisplayText
                update { it.copy(text = nextDisplayText, activeToolName = null) }
            } else {
                update { it.copy(activeToolName = null) }
            }
            return
        }

        if (speechOutput != null) {
            update { it.copy(activeToolName = null) }
            speechOutput.onContent(cleanChunk)
        } else {
            emittedDisplayBuffer = nextDisplayText
            update { it.copy(text = nextDisplayText, activeToolName = null) }
        }
    }

    try {
        val toolCtx = agentToolContext
        val useAgentLoop = toolCtx != null && shouldUseAgentLoopForInput(
            settings = settings,
            history = requestHistory,
            userText = userText,
        )
        val error = if (toolCtx != null && useAgentLoop) {
        // Agent mode: use agent loop with tool support
            val agentHistory = requestHistory
                .filter { it.text.isNotBlank() }
                .takeLast(12)
                .map { message ->
                    AgentMessage(
                        role = if (message.isAgent) "assistant" else "user",
                        content = message.text,
                    )
                }

            runAgentLoop(
                settings = settings,
                mode = mode,
                persona = persona,
                history = agentHistory,
                toolContext = toolCtx,
                onContent = { chunk ->
                    handleContentChunk(chunk)
                },
                onThinking = { chunk ->
                    update { message ->
                        message.copy(
                            thinking = message.thinking + chunk,
                            showThinking = true,
                            thinkingExpanded = message.thinking.isBlank() || message.thinkingExpanded,
                        )
                    }
                },
                onToolCall = { toolName, _ ->
                    update { it.copy(activeToolName = toolName) }
                },
            )
        } else {
        // Regular streaming mode (no tools)
            fetchOpenAiCompatibleLiveStream(
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
                    handleContentChunk(chunk)
                },
            )
        }

        if (error != null) {
            splitForStreaming("请求失败：$error").forEach { chunk ->
                update { it.copy(text = it.text + chunk) }
                delay(18)
            }
        }

        if (error == null) {
            speechOutput?.finish()
        } else {
            speechOutput?.cancel()
        }

        update { message ->
            message.copy(
                streaming = false,
                activeToolName = null,
                thinkingExpanded = false,
                showThinking = message.thinking.isNotBlank(),
            )
        }
        saveChatMessages(context, recordId, messages)
    } catch (cancelled: CancellationException) {
        speechOutput?.cancel()
        withContext(NonCancellable + Dispatchers.Main) {
            update { message ->
                message.copy(
                    text = message.text.ifBlank { "已中断。" },
                    streaming = false,
                    activeToolName = null,
                    thinkingExpanded = false,
                    showThinking = message.thinking.isNotBlank(),
                )
            }
            saveChatMessages(context, recordId, messages)
        }
        throw cancelled
    }
}

private suspend fun shouldUseAgentLoopForInput(
    settings: ModelSettings,
    history: List<ChatMessage>,
    userText: String,
): Boolean = withContext(Dispatchers.IO) {
    if (!settings.useRemote || !settings.provider.compatible) {
        return@withContext looksLikeLocalToolRequest(userText)
    }
    if (settings.apiKey.isBlank() && !settings.provider.name.equals("Ollama", ignoreCase = true)) {
        return@withContext looksLikeLocalToolRequest(userText)
    }

    val base = settings.baseUrl.trim().trimEnd('/')
    val endpoint = "$base/chat/completions"
    val recentContext = history
        .dropLast(1)
        .filter { it.text.isNotBlank() }
        .takeLast(4)
        .joinToString("\n") { message ->
            "${if (message.isAgent) "assistant" else "user"}: ${message.text.take(300)}"
        }
        .ifBlank { "无" }

    val classifierPrompt = """
        你是 Amaduse 移动端 Agent 路由器，只判断当前用户输入是否需要调用本地工具。
        可用工具：
        - test_voice：测试语音合成/播放；用户问“你能说话吗”“能出声吗”“语音正常吗”也算需要。
        - test_emotion：测试 Live2D 表情或动作。
        - set_alarm：按北京时间直接创建 Android 系统闹钟；“30 分钟后/2 小时后”这类相对提醒也算需要。
        - add_memo：在 Android 官方 Calendar 中创建日历任务/事项；缺少标题、开始时间、结束时间时也需要进入 agent，由 agent 继续追问。
        - web_search：联网搜索公开网页；用户要求“搜索/联网查/最新/今天/新闻/查资料/核实/现在是什么情况”等需要外部实时信息时需要。
        - create_local_file：创建并保存 txt/md/csv/docx/xlsx 到本地 Documents/Amaduse；缺少文件名、格式或内容时也需要进入 agent，由 agent 继续追问。
        只有用户明确要求执行这些工具操作、当前输入是在补充工具参数，或问题明显需要实时联网信息时，use_agent 才为 true。
        普通知识问答、角色扮演、闲聊、代码解释、设置咨询，一律 false。
        只输出严格 JSON，不要解释：{"use_agent":true} 或 {"use_agent":false}
    """.trimIndent()

    val payloadMessages = JSONArray()
        .put(JSONObject().put("role", "system").put("content", classifierPrompt))
        .put(
            JSONObject()
                .put("role", "user")
                .put(
                    "content",
                    "最近上下文：\n$recentContext\n\n当前输入：\n$userText",
                ),
        )

    val payload = ChatCompletionModeAdapter.buildPayload(
        config = settings.toChatCompletionModeConfig(),
        mode = ChatCompletionMode.Fast,
        payloadMessages = payloadMessages,
        stream = false,
    )

    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 10_000
        readTimeout = 20_000
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("Accept", "application/json")
        if (settings.apiKey.isNotBlank()) {
            setRequestProperty("Authorization", "Bearer ${settings.apiKey}")
        }
    }

    try {
        connection.outputStream.use { output ->
            output.write(payload.toString().toByteArray(Charsets.UTF_8))
        }
        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            return@withContext looksLikeLocalToolRequest(userText)
        }

        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val content = JSONObject(body)
            .optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("message")
            ?.optString("content")
            .orEmpty()
        parseAgentRouteDecision(content) ?: looksLikeLocalToolRequest(userText)
    } catch (_: Exception) {
        looksLikeLocalToolRequest(userText)
    } finally {
        connection.disconnect()
    }
}

private fun parseAgentRouteDecision(content: String): Boolean? {
    val start = content.indexOf('{')
    val end = content.lastIndexOf('}')
    if (start >= 0 && end > start) {
        val json = runCatching { JSONObject(content.substring(start, end + 1)) }.getOrNull()
        if (json != null && json.has("use_agent")) {
            return json.optBoolean("use_agent", false)
        }
    }
    val normalized = content.lowercase(Locale.ROOT)
    return when {
        "\"use_agent\":true" in normalized || "use_agent:true" in normalized -> true
        "\"use_agent\":false" in normalized || "use_agent:false" in normalized -> false
        else -> null
    }
}

private fun looksLikeLocalToolRequest(text: String): Boolean {
    val normalized = text.lowercase(Locale.ROOT)
    return listOf(
        "闹钟",
        "提醒我",
        "叫醒我",
        "备忘",
        "记一下",
        "记下来",
        "记录一下",
        "你能说话",
        "能说话",
        "出声",
        "语音测试",
        "测试语音",
        "表情",
        "live2d",
        "emotion",
        "搜索",
        "联网",
        "网上",
        "查一下",
        "查找",
        "查资料",
        "核实",
        "最新",
        "新闻",
        "今天",
        "现在",
        "web search",
        "search",
        "创建文件",
        "保存文件",
        "生成文件",
        "写入文件",
        "导出",
        "txt",
        "md",
        "markdown",
        "csv",
        "docx",
        "xlsx",
        "excel",
        "word",
    ).any { it in normalized }
}

private fun String.stripEmotionTagsForDisplay(emotionRegex: Regex): String {
    val withoutCompleteTags = replace(emotionRegex, "")
    val markerStart = withoutCompleteTags.lastIndexOf('[')
    if (markerStart < 0 || markerStart != withoutCompleteTags.length - 1 && '\n' in withoutCompleteTags.substring(markerStart)) {
        return withoutCompleteTags
    }
    val trailing = withoutCompleteTags.substring(markerStart + 1).lowercase(Locale.ROOT)
    val fullMarkerPrefix = "emotion:"
    val isPotentialEmotionMarker = fullMarkerPrefix.startsWith(trailing) || trailing.startsWith(fullMarkerPrefix)
    return if (isPotentialEmotionMarker) {
        withoutCompleteTags.substring(0, markerStart)
    } else {
        withoutCompleteTags
    }
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
                    if (content.isNotEmpty()) {
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
    val time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    }.format(Date())
    return "北京时间 $time"
}
