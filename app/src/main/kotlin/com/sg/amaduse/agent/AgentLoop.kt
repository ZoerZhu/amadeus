package com.sg.amaduse.agent

import com.sg.amaduse.ChatMode
import com.sg.amaduse.ModelSettings
import com.sg.amaduse.agent.model.ChatCompletionMode
import com.sg.amaduse.agent.model.ChatCompletionModeAdapter
import com.sg.amaduse.agent.model.ChatCompletionModeConfig
import com.sg.amaduse.agent.persona.PersonaPreset
import com.sg.amaduse.agent.persona.PersonaPromptBuilder
import com.sg.amaduse.agent.persona.PromptMode
import com.sg.amaduse.agent.persona.PromptRuntimeContext
import com.sg.amaduse.agent.tools.AgentToolContext
import com.sg.amaduse.agent.tools.ToolRegistry
import com.sg.amaduse.agent.tools.ToolResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val MAX_TOOL_ROUNDS = 5

internal data class AgentMessage(
    val role: String,
    val content: String = "",
    val toolCallId: String? = null,
    val toolCalls: List<AgentToolCall>? = null,
    val name: String? = null,
)

internal data class AgentToolCall(
    val id: String,
    val name: String,
    val arguments: String,
)

internal suspend fun runAgentLoop(
    settings: ModelSettings,
    mode: ChatMode,
    persona: PersonaPreset,
    history: List<AgentMessage>,
    toolContext: AgentToolContext,
    onContent: suspend (String) -> Unit,
    onThinking: suspend (String) -> Unit,
    onToolCall: suspend (String, String) -> Unit,
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
    val toolsJson = ToolRegistry.toJsonArray()
    val completionMode = mode.toChatCompletionMode()
    val config = settings.toAgentCompletionConfig()

    val messages = mutableListOf<AgentMessage>()

    // System prompt
    val systemPrompt = PersonaPromptBuilder.build(
        persona = persona,
        context = PromptRuntimeContext(
            userName = "用户",
            currentTimeText = currentAgentTimeText(),
            mode = mode.toPromptMode(),
        ),
    )
    messages.add(AgentMessage(role = "system", content = systemPrompt))

    // History
    messages.addAll(history)

    var round = 0
    while (round < MAX_TOOL_ROUNDS) {
        round++

        val payloadMessages = buildPayloadMessages(messages)
        val payload = ChatCompletionModeAdapter.buildPayload(
            config = config,
            mode = completionMode,
            payloadMessages = payloadMessages,
            tools = toolsJson,
            stream = false,
        )

        val response = callApi(endpoint, settings.apiKey, payload)
        if (response.error != null) {
            return@withContext response.error
        }

        val choice = response.choices?.optJSONObject(0)
        val message = choice?.optJSONObject("message")
        // Parse content
        val content = message?.optString("content") ?: ""
        val reasoning = if (completionMode.shouldReadReasoning) {
            message.cleanString("reasoning_content").ifBlank {
                message.cleanString("reasoning")
            }
        } else {
            ""
        }

        if (reasoning.isNotBlank()) {
            withContext(Dispatchers.Main) { onThinking(reasoning) }
        }

        // Parse tool_calls
        val toolCallsArray = message?.optJSONArray("tool_calls")
        val toolCalls = mutableListOf<AgentToolCall>()
        if (toolCallsArray != null) {
            for (i in 0 until toolCallsArray.length()) {
                val tc = toolCallsArray.optJSONObject(i) ?: continue
                val function = tc.optJSONObject("function") ?: continue
                toolCalls.add(
                    AgentToolCall(
                        id = tc.optString("id", "call_$i"),
                        name = function.optString("name", ""),
                        arguments = function.optString("arguments", "{}"),
                    ),
                )
            }
        }

        // If no tool calls, we're done
        if (toolCalls.isEmpty()) {
            if (content.isNotEmpty()) {
                withContext(Dispatchers.Main) { onContent(content) }
            }
            return@withContext null
        }

        // Add assistant message with tool calls
        messages.add(
            AgentMessage(
                role = "assistant",
                content = content,
                toolCalls = toolCalls,
            ),
        )

        // Stream partial content if any
        if (content.isNotEmpty()) {
            withContext(Dispatchers.Main) { onContent(content) }
        }

        // Execute each tool
        for (tc in toolCalls) {
            withContext(Dispatchers.Main) { onToolCall(tc.name, tc.arguments) }

            val tool = ToolRegistry.find(tc.name)
            val result = if (tool != null) {
                try {
                    val args = runCatching { JSONObject(tc.arguments) }.getOrElse { JSONObject() }
                    tool.execute(args, toolContext)
                } catch (e: Exception) {
                    ToolResult(false, "工具执行异常：${e.message ?: e.javaClass.simpleName}")
                }
            } else {
                ToolResult(false, "未知工具：${tc.name}")
            }

            messages.add(
                AgentMessage(
                    role = "tool",
                    content = JSONObject()
                        .put("success", result.success)
                        .put("output", result.output)
                        .toString(),
                    toolCallId = tc.id,
                    name = tc.name,
                ),
            )
        }
    }

    withContext(Dispatchers.Main) {
        onContent("\n\n[已达到最大工具调用轮次]")
    }
    null
}

private data class ApiResponse(
    val choices: JSONArray?,
    val error: String?,
)

private fun callApi(
    endpoint: String,
    apiKey: String,
    payload: JSONObject,
): ApiResponse {
    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 20_000
        readTimeout = 60_000
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("Accept", "application/json")
        if (apiKey.isNotBlank()) {
            setRequestProperty("Authorization", "Bearer $apiKey")
        }
    }

    return try {
        connection.outputStream.use { output ->
            output.write(payload.toString().toByteArray(Charsets.UTF_8))
        }

        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            return ApiResponse(null, "HTTP $responseCode ${errorText.take(220)}")
        }

        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(body)
        val choices = json.optJSONArray("choices")
        ApiResponse(choices, null)
    } catch (e: Exception) {
        ApiResponse(null, e.message ?: e.javaClass.simpleName)
    } finally {
        connection.disconnect()
    }
}

private fun buildPayloadMessages(messages: List<AgentMessage>): JSONArray {
    val array = JSONArray()
    for (msg in messages) {
        val obj = JSONObject().put("role", msg.role)
        when (msg.role) {
            "system", "user" -> {
                obj.put("content", msg.content)
            }
            "assistant" -> {
                obj.put("content", msg.content)
                if (msg.toolCalls != null && msg.toolCalls.isNotEmpty()) {
                    val tcArray = JSONArray()
                    for (tc in msg.toolCalls) {
                        tcArray.put(
                            JSONObject()
                                .put("id", tc.id)
                                .put("type", "function")
                                .put(
                                    "function", JSONObject()
                                        .put("name", tc.name)
                                        .put("arguments", tc.arguments),
                                ),
                        )
                    }
                    obj.put("tool_calls", tcArray)
                }
            }
            "tool" -> {
                obj.put("content", msg.content)
                obj.put("tool_call_id", msg.toolCallId ?: "")
            }
        }
        array.put(obj)
    }
    return array
}

private fun JSONObject?.cleanString(key: String): String {
    if (this == null || isNull(key)) {
        return ""
    }
    return optString(key).trim()
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

private fun ModelSettings.toAgentCompletionConfig(): ChatCompletionModeConfig {
    return ChatCompletionModeConfig(
        providerName = provider.name,
        providerBaseUrl = provider.baseUrl,
        baseUrl = baseUrl,
        model = model,
        defaultModel = provider.defaultModel,
    )
}

private fun currentAgentTimeText(): String {
    val time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    }.format(Date())
    return "北京时间 $time"
}
