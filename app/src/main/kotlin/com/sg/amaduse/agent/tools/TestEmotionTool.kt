package com.sg.amaduse.agent.tools

import org.json.JSONObject

internal class TestEmotionTool : AgentTool {
    override val name = "test_emotion"
    override val description = "测试 Live2D 角色表情切换。指定情感名称，Live2D 模型会播放对应的表情和动作。"
    override val parameters = JSONObject()
        .put("type", "object")
        .put(
            "properties", JSONObject().put(
                "emotion", JSONObject()
                    .put("type", "string")
                    .put("description", "情感名称")
                    .put(
                        "enum",
                        org.json.JSONArray(
                            listOf(
                                "neutral",
                                "anger",
                                "joy",
                                "sadness",
                                "shy",
                                "smile",
                                "surprise",
                                "unhappy",
                            ),
                        ),
                    ),
            ),
        )
        .put("required", org.json.JSONArray(listOf("emotion")))

    override suspend fun execute(args: JSONObject, context: AgentToolContext): ToolResult {
        val emotion = args.optString("emotion", "")
        if (emotion.isBlank()) {
            return ToolResult(false, "情感测试失败：未指定情感名称。")
        }
        val validEmotions = setOf("neutral", "anger", "joy", "sadness", "shy", "smile", "surprise", "unhappy")
        if (emotion !in validEmotions) {
            return ToolResult(false, "情感测试失败：无效的情感名称「$emotion」。可用：${validEmotions.joinToString(", ")}")
        }
        val webView = context.live2dWebView
        if (webView == null) {
            return ToolResult(false, "情感测试失败：Live2D 画布未就绪。")
        }
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            webView.evaluateJavascript("playEmotion('$emotion')", null)
            ToolResult(true, "情感测试成功：已切换到「$emotion」表情。")
        }
    }
}
