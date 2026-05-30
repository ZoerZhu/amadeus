package com.sg.amaduse

import android.content.Context
import android.net.Uri
import com.sg.amaduse.agent.audio.DEFAULT_VOICE_REFERENCE_SOURCE
import com.sg.amaduse.agent.audio.VoiceSettings
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

internal fun defaultModelSettings(): ModelSettings {
    val provider = modelProviderPresets.first { it.name == "OpenAI" }
    return ModelSettings(
        configuredModelId = "",
        provider = provider,
        model = provider.defaultModel,
        baseUrl = provider.baseUrl,
        apiKey = "",
        useRemote = true,
    )
}

internal fun ConfiguredModel.toModelSettings(): ModelSettings {
    return ModelSettings(
        configuredModelId = id,
        provider = provider,
        model = model,
        baseUrl = baseUrl,
        apiKey = apiKey,
        useRemote = apiKey.isNotBlank(),
    )
}

internal fun loadConfiguredModels(context: Context): List<ConfiguredModel> {
    val prefs = context.getSharedPreferences("amaduse_settings", Context.MODE_PRIVATE)
    val saved = prefs.getString("configured_models", "").orEmpty()
    val loaded = runCatching {
        if (saved.isBlank()) {
            emptyList()
        } else {
            val array = JSONArray(saved)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                val providerName = item.optString("provider")
                val provider = modelProviderPresets.firstOrNull { it.name == providerName }
                    ?: modelProviderPresets.first { it.name == "自定义" }
                ConfiguredModel(
                    id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                    displayName = item.optString("display_name").ifBlank { provider.name },
                    provider = provider,
                    model = item.optString("model").ifBlank { provider.defaultModel },
                    baseUrl = item.optString("base_url").ifBlank { provider.baseUrl },
                    apiKey = item.optString("api_key"),
                )
            }
        }
    }.getOrDefault(emptyList())

    if (loaded.isNotEmpty()) {
        return loaded
    }

    val legacyApiKey = prefs.getString("api_key", "").orEmpty()
    if (legacyApiKey.isBlank()) {
        return emptyList()
    }
    val providerName = prefs.getString("provider", "OpenAI").orEmpty()
    val provider = modelProviderPresets.firstOrNull { it.name == providerName && it.name != "演示模型" }
        ?: modelProviderPresets.first { it.name == "OpenAI" }
    val migrated = listOf(
        ConfiguredModel(
            displayName = provider.name,
            provider = provider,
            model = prefs.getString("model", provider.defaultModel).orEmpty().ifBlank { provider.defaultModel },
            baseUrl = prefs.getString("base_url", provider.baseUrl).orEmpty().ifBlank { provider.baseUrl },
            apiKey = legacyApiKey,
        ),
    )
    saveConfiguredModels(context, migrated)
    return migrated
}

internal fun saveConfiguredModels(context: Context, configuredModels: List<ConfiguredModel>) {
    val array = JSONArray()
    configuredModels.forEach { item ->
        array.put(
            JSONObject()
                .put("id", item.id)
                .put("display_name", item.displayName)
                .put("provider", item.provider.name)
                .put("model", item.model)
                .put("base_url", item.baseUrl)
                .put("api_key", item.apiKey),
        )
    }
    context.getSharedPreferences("amaduse_settings", Context.MODE_PRIVATE)
        .edit()
        .putString("configured_models", array.toString())
        .apply()
}

internal fun loadModelSettings(context: Context, configuredModels: List<ConfiguredModel>): ModelSettings {
    val prefs = context.getSharedPreferences("amaduse_settings", Context.MODE_PRIVATE)
    val selectedId = prefs.getString("selected_model_id", "").orEmpty()
    configuredModels.firstOrNull { it.id == selectedId }?.let {
        return it.toModelSettings()
    }
    configuredModels.firstOrNull { it.apiKey.isNotBlank() }?.let {
        return it.toModelSettings()
    }

    val fallback = defaultModelSettings()
    val providerName = prefs.getString("provider", fallback.provider.name).orEmpty()
    val provider = modelProviderPresets.firstOrNull { it.name == providerName && it.name != "演示模型" }
        ?: fallback.provider
    return ModelSettings(
        configuredModelId = "",
        provider = provider,
        model = prefs.getString("model", provider.defaultModel).orEmpty().ifBlank { provider.defaultModel },
        baseUrl = prefs.getString("base_url", provider.baseUrl).orEmpty().ifBlank { provider.baseUrl },
        apiKey = prefs.getString("api_key", "").orEmpty(),
        useRemote = prefs.getBoolean("use_remote", provider.compatible),
    )
}

internal fun saveModelSettings(context: Context, settings: ModelSettings) {
    context.getSharedPreferences("amaduse_settings", Context.MODE_PRIVATE)
        .edit()
        .putString("selected_model_id", settings.configuredModelId)
        .putString("provider", settings.provider.name)
        .putString("model", settings.model)
        .putString("base_url", settings.baseUrl)
        .putString("api_key", settings.apiKey)
        .putBoolean("use_remote", settings.useRemote)
        .apply()
}

internal fun loadVoiceSettings(context: Context): VoiceSettings {
    val prefs = context.getSharedPreferences("amaduse_voice_settings", Context.MODE_PRIVATE)
    return VoiceSettings(
        siliconFlowApiKey = prefs.getString("siliconflow_api_key", "").orEmpty(),
        referenceAudioSource = prefs.getString("reference_audio_source", DEFAULT_VOICE_REFERENCE_SOURCE)
            .orEmpty()
            .ifBlank { DEFAULT_VOICE_REFERENCE_SOURCE },
        clonedVoiceUri = prefs.getString("cloned_voice_uri", "").orEmpty(),
        autoPlay = prefs.getBoolean("auto_play", false),
        syncTextOutput = prefs.getBoolean("sync_text_output", true),
    )
}

internal fun saveVoiceSettings(context: Context, settings: VoiceSettings) {
    context.getSharedPreferences("amaduse_voice_settings", Context.MODE_PRIVATE)
        .edit()
        .putString("siliconflow_api_key", settings.siliconFlowApiKey)
        .putString("reference_audio_source", settings.referenceAudioSource.ifBlank { DEFAULT_VOICE_REFERENCE_SOURCE })
        .putString("cloned_voice_uri", settings.clonedVoiceUri)
        .putBoolean("auto_play", settings.autoPlay)
        .putBoolean("sync_text_output", settings.syncTextOutput)
        .apply()
}

internal fun loadChatMode(context: Context): ChatMode {
    val value = context.getSharedPreferences("amaduse_settings", Context.MODE_PRIVATE)
        .getString("chat_mode", ChatMode.Fast.name)
    return runCatching { ChatMode.valueOf(value ?: ChatMode.Fast.name) }.getOrDefault(ChatMode.Fast)
}

internal fun saveChatMode(context: Context, mode: ChatMode) {
    context.getSharedPreferences("amaduse_settings", Context.MODE_PRIVATE)
        .edit()
        .putString("chat_mode", mode.name)
        .apply()
}

internal fun loadChatRecords(context: Context): List<ChatRecord> {
    val file = chatRecordsFile(context)
    val records = runCatching {
        if (!file.exists()) {
            emptyList()
        } else {
            val array = JSONArray(file.readText())
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                ChatRecord(
                    title = item.optString("title").ifBlank { "新聊天" },
                    subtitle = item.optString("subtitle"),
                    active = item.optBoolean("active", false),
                    id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                )
            }
        }
    }.getOrDefault(emptyList())

    if (records.isNotEmpty()) {
        return records
    }

    val initial = listOf(createNewChatRecord())
    saveChatRecords(context, initial)
    return initial
}

internal fun saveChatRecords(context: Context, records: List<ChatRecord>) {
    val array = JSONArray()
    records.forEach { record ->
        array.put(
            JSONObject()
                .put("id", record.id)
                .put("title", record.title)
                .put("subtitle", record.subtitle)
                .put("active", record.active),
        )
    }
    chatRecordsFile(context).writeText(array.toString())
}

internal fun loadChatMessages(context: Context, recordId: String): List<ChatMessage> {
    val file = chatMessagesFile(context, recordId)
    return runCatching {
        if (!file.exists()) {
            emptyList()
        } else {
            val array = JSONArray(file.readText())
            List(array.length()) { index ->
                jsonToMessage(array.getJSONObject(index))
            }
        }
    }.getOrDefault(emptyList())
}

internal fun saveChatMessages(context: Context, recordId: String, messages: List<ChatMessage>) {
    val array = JSONArray()
    messages.forEach { message ->
        array.put(messageToJson(message))
    }
    chatMessagesFile(context, recordId).writeText(array.toString())
}

private fun messageToJson(message: ChatMessage): JSONObject {
    val attachments = JSONArray()
    message.attachments.forEach { attachment ->
        attachments.put(
            JSONObject()
                .put("uri", attachment.uri.toString())
                .put("label", attachment.label)
                .put("kind", attachment.kind.name),
        )
    }
    return JSONObject()
        .put("author", message.author)
        .put("text", message.text)
        .put("is_agent", message.isAgent)
        .put("time", message.time)
        .put("thinking", message.thinking)
        .put("show_thinking", message.showThinking)
        .put("thinking_expanded", message.thinkingExpanded)
        .put("streaming", false)
        .put("active_tool_name", JSONObject.NULL)
        .put("mode", message.mode.name)
        .put("attachments", attachments)
}

private fun jsonToMessage(json: JSONObject): ChatMessage {
    val attachmentsArray = json.optJSONArray("attachments") ?: JSONArray()
    val attachments = List(attachmentsArray.length()) { index ->
        val item = attachmentsArray.getJSONObject(index)
        ComposerAttachment(
            uri = Uri.parse(item.optString("uri")),
            label = item.optString("label").ifBlank { "附件" },
            kind = runCatching {
                AttachmentKind.valueOf(item.optString("kind"))
            }.getOrDefault(AttachmentKind.File),
        )
    }
    return ChatMessage(
        author = json.optString("author").ifBlank { "Amaduse" },
        text = cleanDisplayText(json.optString("text")),
        isAgent = json.optBoolean("is_agent", true),
        time = json.optString("time").ifBlank { currentTimeText() },
        attachments = attachments,
        thinking = cleanDisplayText(json.optString("thinking")),
        showThinking = json.optBoolean("show_thinking", false),
        thinkingExpanded = json.optBoolean("thinking_expanded", false),
        streaming = false,
        activeToolName = null,
        mode = runCatching {
            ChatMode.valueOf(json.optString("mode"))
        }.getOrDefault(ChatMode.Fast),
    )
}

internal fun updateRecordPreview(
    records: MutableList<ChatRecord>,
    current: ChatRecord,
    latestText: String,
): ChatRecord {
    val nextTitle = if (current.title == "新聊天") {
        latestText.trim().take(24).ifBlank { "新聊天" }
    } else {
        current.title
    }
    val next = current.copy(
        title = nextTitle,
        subtitle = latestText.trim().take(56),
        active = true,
    )
    val index = records.indexOfFirst { it.id == current.id }
    if (index >= 0) {
        records[index] = next
    }
    return next
}

internal fun createNewChatRecord(): ChatRecord {
    return ChatRecord(
        title = "新聊天",
        subtitle = currentTimeText(),
        active = true,
        id = UUID.randomUUID().toString(),
    )
}

internal fun currentTimeText(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    }.format(Date())
}

private fun chatRecordsFile(context: Context): File {
    return File(context.filesDir, "amaduse_chats.json")
}

private fun chatMessagesFile(context: Context, recordId: String): File {
    val dir = File(context.filesDir, "amaduse_messages")
    if (!dir.exists()) {
        dir.mkdirs()
    }
    return File(dir, "$recordId.json")
}
