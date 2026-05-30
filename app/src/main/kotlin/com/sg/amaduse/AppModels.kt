package com.sg.amaduse

import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector
import com.sg.amaduse.agent.persona.PersonaPreset
import java.util.UUID

internal data class ChatMessage(
    val author: String,
    val text: String,
    val isAgent: Boolean,
    val time: String,
    val attachments: List<ComposerAttachment> = emptyList(),
    val toolPreview: ToolPreview? = null,
    val thinking: String = "",
    val showThinking: Boolean = false,
    val thinkingExpanded: Boolean = false,
    val streaming: Boolean = false,
    val activeToolName: String? = null,
    val mode: ChatMode = ChatMode.Fast,
)

internal data class ToolPreview(
    val title: String,
    val detail: String,
    val risk: String,
)

internal data class ComposerAttachment(
    val uri: Uri,
    val label: String,
    val kind: AttachmentKind,
)

internal data class ToolShortcut(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

internal data class ModelProviderPreset(
    val name: String,
    val baseUrl: String,
    val defaultModel: String,
    val compatible: Boolean,
    val note: String,
)

internal data class ModelSettings(
    val configuredModelId: String = "",
    val provider: ModelProviderPreset,
    val model: String,
    val baseUrl: String,
    val apiKey: String,
    val useRemote: Boolean,
)

internal data class ConfiguredModel(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String,
    val provider: ModelProviderPreset,
    val model: String,
    val baseUrl: String,
    val apiKey: String,
)

internal enum class AttachmentKind {
    Image,
    File,
}

internal enum class ChatMode(val label: String, val caption: String) {
    Fast("快速", "更短延迟，直接回答"),
    Thinking("思考", "先展示推理摘要，再回答"),
}

internal enum class AppScreen {
    Chat,
    History,
}

internal enum class SettingsSheetMode {
    Full,
    ModelOnly,
}

internal data class ChatRecord(
    val title: String,
    val subtitle: String,
    val active: Boolean = false,
    val id: String = UUID.randomUUID().toString(),
)

internal data class ChatUiState(
    val selectedPersona: PersonaPreset,
    val selectedMode: ChatMode,
)
