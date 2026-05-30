package com.sg.amaduse.agent.tools

import com.sg.amaduse.agent.audio.SiliconFlowVoiceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal class TestVoiceTool : AgentTool {
    override val name = "test_voice"
    override val description = "测试语音合成功能是否正常。调用 TTS 接口生成一段测试语音并播放，返回连通性结果。"
    override val parameters = JSONObject()
        .put("type", "object")
        .put("properties", JSONObject())
        .put("required", org.json.JSONArray())

    override suspend fun execute(args: JSONObject, context: AgentToolContext): ToolResult {
        val apiKey = context.voiceSettings.siliconFlowApiKey
        if (apiKey.isBlank()) {
            return ToolResult(false, "语音测试失败：未配置 SiliconFlow API Key。请在设置中填写。")
        }
        val voice = context.voiceSettings.clonedVoiceUri.ifBlank {
            "speech:siliconflow-kurisu:clzv7bjjm041fufyct2z0setm:mphrsbbmvrjfophbsted"
        }
        return withContext(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            val result = runCatching {
                SiliconFlowVoiceService.synthesizeSpeechToCacheFile(
                    context = context.appContext,
                    apiKey = apiKey,
                    input = "连接测试成功，语音功能正常。",
                    voice = voice,
                )
            }
            result.fold(
                onSuccess = { file ->
                    val elapsed = System.currentTimeMillis() - start
                    SiliconFlowVoiceService.playAudioFile(file)
                    ToolResult(true, "语音测试成功。TTS 接口连通，耗时 ${elapsed}ms，音频已播放。")
                },
                onFailure = { e ->
                    ToolResult(false, "语音测试失败：${e.message ?: e.javaClass.simpleName}")
                },
            )
        }
    }
}
