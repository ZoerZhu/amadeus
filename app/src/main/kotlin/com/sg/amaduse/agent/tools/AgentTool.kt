package com.sg.amaduse.agent.tools

import android.content.Context
import android.webkit.WebView
import com.sg.amaduse.ModelSettings
import com.sg.amaduse.agent.audio.VoiceSettings
import org.json.JSONObject

internal interface AgentTool {
    val name: String
    val description: String
    val parameters: JSONObject
    suspend fun execute(args: JSONObject, context: AgentToolContext): ToolResult
}

internal data class ToolResult(
    val success: Boolean,
    val output: String,
)

internal data class AgentToolContext(
    val appContext: Context,
    val voiceSettings: VoiceSettings,
    val modelSettings: ModelSettings,
    val live2dWebView: WebView?,
    val requestCalendarPermissions: suspend () -> Boolean = { false },
)
