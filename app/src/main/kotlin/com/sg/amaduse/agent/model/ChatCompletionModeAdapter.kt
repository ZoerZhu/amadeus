package com.sg.amaduse.agent.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

internal enum class ChatCompletionMode {
    Fast,
    Thinking;

    val shouldReadReasoning: Boolean
        get() = this == Thinking
}

internal data class ChatCompletionModeConfig(
    val providerName: String,
    val providerBaseUrl: String,
    val baseUrl: String,
    val model: String,
    val defaultModel: String,
)

internal object ChatCompletionModeAdapter {
    fun buildPayload(
        config: ChatCompletionModeConfig,
        mode: ChatCompletionMode,
        payloadMessages: JSONArray,
    ): JSONObject {
        val requestModel = resolveRequestModel(config, mode)
        val payload = JSONObject()
            .put("model", requestModel)
            .put("stream", true)
            .put("messages", payloadMessages)

        applyModeParameters(payload, config, mode, requestModel)
        return payload
    }

    private fun resolveRequestModel(
        config: ChatCompletionModeConfig,
        mode: ChatCompletionMode,
    ): String {
        val model = config.model.ifBlank { config.defaultModel }
        if (!config.isDeepSeekProvider()) {
            return model
        }

        return when {
            mode == ChatCompletionMode.Fast && model.contains("reasoner", ignoreCase = true) -> "deepseek-chat"
            mode == ChatCompletionMode.Thinking && model.equals("deepseek-chat", ignoreCase = true) -> "deepseek-reasoner"
            else -> model
        }
    }

    private fun applyModeParameters(
        payload: JSONObject,
        config: ChatCompletionModeConfig,
        mode: ChatCompletionMode,
        requestModel: String,
    ) {
        when {
            config.isMiMoProvider(requestModel) -> applyMiMoModeParameters(payload, mode)
            config.isOpenRouterProvider() -> applyOpenRouterModeParameters(payload, mode)
            config.isDashScopeProvider(requestModel) -> applyDashScopeModeParameters(payload, mode)
            config.isDeepSeekProvider() -> applyDeepSeekModeParameters(payload, mode, requestModel)
            config.isOpenAiProvider(requestModel) -> applyOpenAiModeParameters(payload, mode, requestModel)
            else -> applyGenericModeParameters(payload, mode)
        }
    }

    private fun applyMiMoModeParameters(
        payload: JSONObject,
        mode: ChatCompletionMode,
    ) {
        payload.put(
            "thinking",
            JSONObject().put("type", if (mode == ChatCompletionMode.Thinking) "enabled" else "disabled"),
        )
        payload.put("top_p", 0.95)
        if (mode == ChatCompletionMode.Fast) {
            payload.put("temperature", 0.7)
            payload.put("max_completion_tokens", 1024)
        } else {
            payload.put("max_completion_tokens", 4096)
        }
    }

    private fun applyOpenRouterModeParameters(
        payload: JSONObject,
        mode: ChatCompletionMode,
    ) {
        if (mode == ChatCompletionMode.Fast) {
            payload.put(
                "reasoning",
                JSONObject()
                    .put("effort", "none")
                    .put("exclude", true),
            )
            payload.put("max_tokens", 1024)
            payload.put("temperature", 0.7)
        } else {
            payload.put(
                "reasoning",
                JSONObject()
                    .put("effort", "medium")
                    .put("exclude", false),
            )
            payload.put("max_tokens", 4096)
        }
    }

    private fun applyDashScopeModeParameters(
        payload: JSONObject,
        mode: ChatCompletionMode,
    ) {
        payload.put("enable_thinking", mode == ChatCompletionMode.Thinking)
        if (mode == ChatCompletionMode.Thinking) {
            payload.put("thinking_budget", 1024)
            payload.put("max_tokens", 4096)
        } else {
            payload.put("max_tokens", 1024)
            payload.put("temperature", 0.7)
        }
    }

    private fun applyDeepSeekModeParameters(
        payload: JSONObject,
        mode: ChatCompletionMode,
        requestModel: String,
    ) {
        if (mode == ChatCompletionMode.Fast) {
            payload.put("max_tokens", 1024)
            if (!requestModel.contains("reasoner", ignoreCase = true)) {
                payload.put("temperature", 0.7)
            }
        } else {
            payload.put("max_tokens", 4096)
        }
    }

    private fun applyOpenAiModeParameters(
        payload: JSONObject,
        mode: ChatCompletionMode,
        requestModel: String,
    ) {
        if (requestModel.isOpenAiReasoningModel()) {
            payload.put("reasoning_effort", if (mode == ChatCompletionMode.Thinking) "medium" else "minimal")
            payload.put("max_completion_tokens", if (mode == ChatCompletionMode.Thinking) 4096 else 1024)
        } else {
            payload.put("max_tokens", if (mode == ChatCompletionMode.Thinking) 2048 else 1024)
            if (mode == ChatCompletionMode.Fast) {
                payload.put("temperature", 0.7)
            }
        }
    }

    private fun applyGenericModeParameters(
        payload: JSONObject,
        mode: ChatCompletionMode,
    ) {
        payload.put("max_tokens", if (mode == ChatCompletionMode.Thinking) 2048 else 1024)
        if (mode == ChatCompletionMode.Fast) {
            payload.put("temperature", 0.7)
        }
    }

    private fun ChatCompletionModeConfig.isMiMoProvider(requestModel: String): Boolean {
        val haystack = listOf(providerName, providerBaseUrl, baseUrl, requestModel)
            .joinToString(" ")
            .lowercase(Locale.ROOT)
        return "mimo" in haystack || "xiaomi" in haystack || "小米" in haystack
    }

    private fun ChatCompletionModeConfig.isOpenRouterProvider(): Boolean {
        return providerName.equals("OpenRouter", ignoreCase = true) ||
            "openrouter.ai" in baseUrl.lowercase(Locale.ROOT)
    }

    private fun ChatCompletionModeConfig.isDashScopeProvider(requestModel: String): Boolean {
        val haystack = listOf(providerName, providerBaseUrl, baseUrl, requestModel)
            .joinToString(" ")
            .lowercase(Locale.ROOT)
        return "dashscope" in haystack || "qwen" in haystack || "通义" in haystack
    }

    private fun ChatCompletionModeConfig.isDeepSeekProvider(): Boolean {
        val haystack = listOf(providerName, providerBaseUrl, baseUrl, model)
            .joinToString(" ")
            .lowercase(Locale.ROOT)
        return "deepseek" in haystack
    }

    private fun ChatCompletionModeConfig.isOpenAiProvider(requestModel: String): Boolean {
        val haystack = listOf(providerName, providerBaseUrl, baseUrl, requestModel)
            .joinToString(" ")
            .lowercase(Locale.ROOT)
        return "openai" in haystack || "api.openai.com" in haystack
    }

    private fun String.isOpenAiReasoningModel(): Boolean {
        val normalized = lowercase(Locale.ROOT)
        return normalized.startsWith("o1") ||
            normalized.startsWith("o3") ||
            normalized.startsWith("o4") ||
            normalized.startsWith("gpt-5")
    }
}
