package com.sg.amaduse

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sg.amaduse.ui.theme.AmaduseMotion
import com.sg.amaduse.ui.theme.AmaduseStyle
import com.sg.amaduse.ui.theme.AmaduseTheme
import com.sg.amaduse.ui.theme.glassLayer
import com.sg.amaduse.ui.theme.iconGlass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AmaduseTheme {
                AmaduseApp()
            }
        }
    }
}

private data class PersonaPreset(
    val name: String,
    val subtitle: String,
    val tone: String,
)

private data class ChatMessage(
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
    val mode: ChatMode = ChatMode.Fast,
)

private data class ToolPreview(
    val title: String,
    val detail: String,
    val risk: String,
)

private data class ComposerAttachment(
    val uri: Uri,
    val label: String,
    val kind: AttachmentKind,
)

private data class ToolShortcut(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

private data class ModelProviderPreset(
    val name: String,
    val baseUrl: String,
    val defaultModel: String,
    val compatible: Boolean,
    val note: String,
)

private data class ModelSettings(
    val configuredModelId: String = "",
    val provider: ModelProviderPreset,
    val model: String,
    val baseUrl: String,
    val apiKey: String,
    val useRemote: Boolean,
)

private data class ConfiguredModel(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String,
    val provider: ModelProviderPreset,
    val model: String,
    val baseUrl: String,
    val apiKey: String,
)

private enum class AttachmentKind {
    Image,
    File,
}

private enum class ChatMode(val label: String, val caption: String) {
    Fast("快速", "更短延迟，直接回答"),
    Thinking("思考", "先展示推理摘要，再回答"),
}

private enum class AppScreen {
    Chat,
    History,
}

private enum class SettingsSheetMode {
    Full,
    ModelOnly,
}

private data class ChatRecord(
    val title: String,
    val subtitle: String,
    val active: Boolean = false,
    val id: String = UUID.randomUUID().toString(),
)

private val personaPresets = listOf(
    PersonaPreset(
        name = "Amaduse",
        subtitle = "默认人格",
        tone = "冷静、敏锐、带一点实验室感",
    ),
    PersonaPreset(
        name = "冷静观察者",
        subtitle = "理性辅助",
        tone = "先结论，再推理，少量吐槽",
    ),
    PersonaPreset(
        name = "温柔执行官",
        subtitle = "陪伴与任务",
        tone = "温和、清楚、主动确认风险",
    ),
    PersonaPreset(
        name = "高效秘书",
        subtitle = "日程与事务",
        tone = "短句、结构化、直接给下一步",
    ),
)

private val modelProviderPresets = listOf(
    ModelProviderPreset(
        name = "演示模型",
        baseUrl = "local://demo",
        defaultModel = "amaduse-demo-stream",
        compatible = false,
        note = "无需 Key，本地生成流式演示回复",
    ),
    ModelProviderPreset(
        name = "OpenAI",
        baseUrl = "https://api.openai.com/v1",
        defaultModel = "gpt-4.1-mini",
        compatible = true,
        note = "Chat Completions 兼容流式接口",
    ),
    ModelProviderPreset(
        name = "OpenRouter",
        baseUrl = "https://openrouter.ai/api/v1",
        defaultModel = "openai/gpt-4.1-mini",
        compatible = true,
        note = "聚合多家模型，使用 OpenAI-compatible 协议",
    ),
    ModelProviderPreset(
        name = "DeepSeek",
        baseUrl = "https://api.deepseek.com/v1",
        defaultModel = "deepseek-chat",
        compatible = true,
        note = "可切换 deepseek-reasoner 获取 reasoning_content",
    ),
    ModelProviderPreset(
        name = "通义千问",
        baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1",
        defaultModel = "qwen-plus",
        compatible = true,
        note = "DashScope 兼容模式",
    ),
    ModelProviderPreset(
        name = "Kimi",
        baseUrl = "https://api.moonshot.cn/v1",
        defaultModel = "moonshot-v1-8k",
        compatible = true,
        note = "Moonshot OpenAI-compatible 接口",
    ),
    ModelProviderPreset(
        name = "智谱 GLM",
        baseUrl = "https://open.bigmodel.cn/api/paas/v4",
        defaultModel = "glm-4-flash",
        compatible = true,
        note = "BigModel 兼容接口",
    ),
    ModelProviderPreset(
        name = "Ollama",
        baseUrl = "http://10.0.2.2:11434/v1",
        defaultModel = "qwen2.5:7b",
        compatible = true,
        note = "模拟器访问宿主机本地 Ollama",
    ),
    ModelProviderPreset(
        name = "Anthropic",
        baseUrl = "https://api.anthropic.com/v1",
        defaultModel = "claude-3-5-haiku-latest",
        compatible = false,
        note = "已提供配置项，专用协议后续接入",
    ),
    ModelProviderPreset(
        name = "Gemini",
        baseUrl = "https://generativelanguage.googleapis.com/v1beta",
        defaultModel = "gemini-2.5-flash",
        compatible = false,
        note = "已提供配置项，专用协议后续接入",
    ),
    ModelProviderPreset(
        name = "自定义",
        baseUrl = "https://example.com/v1",
        defaultModel = "custom-model",
        compatible = true,
        note = "填写任意 OpenAI-compatible 服务",
    ),
)

private val toolShortcuts = listOf(
    ToolShortcut("相机", "拍照提问", Icons.Rounded.CameraAlt),
    ToolShortcut("图片", "上传视觉上下文", Icons.Rounded.Image),
    ToolShortcut("文件", "附加资料", Icons.Rounded.AttachFile),
    ToolShortcut("闹钟", "创建提醒", Icons.Rounded.Alarm),
    ToolShortcut("备忘", "记录想法", Icons.Rounded.Description),
    ToolShortcut("电脑", "发送 PC 任务", Icons.Rounded.Computer),
)

private val chatRecords = listOf(
    ChatRecord("Amaduse 前端布局", "当前会话 · 模型和思考模式调试", active = true),
    ChatRecord("LL(1) 文法介绍", "编译原理笔记"),
    ChatRecord("Cursor 配置 DeepSeek API", "OpenAI-compatible 设置"),
    ChatRecord("VPN 访问国内加速", "网络配置排查"),
    ChatRecord("Codex MCP 配置推荐", "工具链与插件"),
    ChatRecord("Greeting Exchange", "英语对话练习"),
    ChatRecord("Claude Code 小米 MiMo 配置问题", "模型服务兼容"),
    ChatRecord("Windows 下载命令错误", "PowerShell 故障分析"),
    ChatRecord("OpenCode 卸载插件方法", "CLI 维护"),
    ChatRecord("轻量实验选题建议", "研究方向整理"),
    ChatRecord("程序运行失败原因分析", "日志和堆栈定位"),
    ChatRecord("GPT Plus 使用限制", "订阅和额度"),
)

@Composable
private fun AmaduseApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val records = remember(context) {
        mutableStateListOf<ChatRecord>().apply {
            addAll(loadChatRecords(context))
        }
    }
    val configuredModels = remember(context) {
        mutableStateListOf<ConfiguredModel>().apply {
            addAll(loadConfiguredModels(context))
        }
    }
    var draft by remember { mutableStateOf("") }
    var appScreen by remember { mutableStateOf(AppScreen.Chat) }
    var selectedRecord by remember { mutableStateOf(records.first()) }
    var selectedPersona by remember { mutableStateOf(personaPresets.first()) }
    var selectedMode by remember { mutableStateOf(loadChatMode(context)) }
    var modelSettings by remember { mutableStateOf(loadModelSettings(context, configuredModels)) }
    var personaSheetVisible by remember { mutableStateOf(false) }
    var settingsVisible by remember { mutableStateOf(false) }
    var settingsSheetMode by remember { mutableStateOf(SettingsSheetMode.Full) }
    var toolsVisible by remember { mutableStateOf(false) }
    var characterExpanded by remember { mutableStateOf(true) }
    var recording by remember { mutableStateOf(false) }
    val attachments = remember { mutableStateListOf<ComposerAttachment>() }
    val messages = remember(context) {
        mutableStateListOf<ChatMessage>().apply {
            addAll(loadChatMessages(context, selectedRecord.id))
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            attachments += ComposerAttachment(
                uri = it,
                label = "图片 ${attachments.count { item -> item.kind == AttachmentKind.Image } + 1}",
                kind = AttachmentKind.Image,
            )
        }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            attachments += ComposerAttachment(
                uri = it,
                label = "文件 ${attachments.count { item -> item.kind == AttachmentKind.File } + 1}",
                kind = AttachmentKind.File,
            )
        }
    }
    val sending = messages.any { it.streaming }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = appScreen,
                transitionSpec = {
                    if (targetState == AppScreen.History) {
                        slideInHorizontally(tween(AmaduseMotion.Default)) { -it } + fadeIn(tween(AmaduseMotion.Default)) togetherWith
                            slideOutHorizontally(tween(AmaduseMotion.Default)) { it / 2 } + fadeOut(tween(AmaduseMotion.Fast))
                    } else {
                        slideInHorizontally(tween(AmaduseMotion.Default)) { it } + fadeIn(tween(AmaduseMotion.Default)) togetherWith
                            slideOutHorizontally(tween(AmaduseMotion.Default)) { -it / 2 } + fadeOut(tween(AmaduseMotion.Fast))
                    } using SizeTransform(clip = false)
                },
                label = "app-screen",
                modifier = Modifier.fillMaxSize(),
            ) { screen ->
                if (screen == AppScreen.History) {
                    ChatHistoryScreen(
                        records = records,
                        selectedRecord = selectedRecord,
                        onAvatarClick = {
                            settingsSheetMode = SettingsSheetMode.Full
                            settingsVisible = true
                        },
                        onNewChat = {
                            val newRecord = createNewChatRecord()
                            records.add(0, newRecord)
                            selectedRecord = newRecord
                            messages.clear()
                            saveChatRecords(context, records)
                            saveChatMessages(context, newRecord.id, messages)
                            characterExpanded = true
                            appScreen = AppScreen.Chat
                        },
                        onRecordClick = { record ->
                            selectedRecord = record
                            messages.clear()
                            messages += loadChatMessages(context, record.id)
                            appScreen = AppScreen.Chat
                        },
                        onDismiss = { appScreen = AppScreen.Chat },
                    )
                } else {
                    ChatScreen(
                        selectedPersona = selectedPersona,
                        settings = modelSettings,
                        configuredModels = configuredModels,
                        selectedMode = selectedMode,
                        sending = sending,
                        messages = messages,
                        characterExpanded = characterExpanded,
                        recording = recording,
                        draft = draft,
                        attachments = attachments,
                        toolsVisible = toolsVisible,
                        onHistoryOpen = { appScreen = AppScreen.History },
                        onPersonaClick = { personaSheetVisible = true },
                        onNewChat = {
                            val newRecord = createNewChatRecord()
                            records.add(0, newRecord)
                            selectedRecord = newRecord
                            messages.clear()
                            saveChatRecords(context, records)
                            saveChatMessages(context, newRecord.id, messages)
                            characterExpanded = true
                        },
                        onModeChange = {
                            selectedMode = it
                            saveChatMode(context, it)
                        },
                        onSettingsClick = {
                            settingsSheetMode = SettingsSheetMode.Full
                            settingsVisible = true
                        },
                        onModelSelect = { configured ->
                            val nextSettings = configured.toModelSettings()
                            modelSettings = nextSettings
                            saveModelSettings(context, nextSettings)
                        },
                        onModelConfigure = {
                            settingsSheetMode = SettingsSheetMode.ModelOnly
                            settingsVisible = true
                        },
                        onCharacterToggle = { characterExpanded = !characterExpanded },
                        onToggleThinking = { index ->
                            if (index in messages.indices) {
                                messages[index] = messages[index].copy(
                                    thinkingExpanded = !messages[index].thinkingExpanded,
                                )
                                saveChatMessages(context, selectedRecord.id, messages)
                            }
                        },
                        onDraftChange = { draft = it },
                        onToolsToggle = { toolsVisible = !toolsVisible },
                        onImagePick = { imagePicker.launch("image/*") },
                        onFilePick = { filePicker.launch("*/*") },
                        onVoiceToggle = { recording = !recording },
                        onRemoveAttachment = { attachments.remove(it) },
                        onToolClick = { shortcut ->
                            when (shortcut.title) {
                                "图片" -> imagePicker.launch("image/*")
                                "文件" -> filePicker.launch("*/*")
                                else -> {
                                    draft = when (shortcut.title) {
                                        "相机" -> "帮我看一下摄像头里的内容"
                                        "闹钟" -> "帮我设置一个闹钟"
                                        "备忘" -> "帮我记录一个备忘"
                                        "电脑" -> "帮我把这个任务发送到电脑"
                                        else -> draft
                                    }
                                    toolsVisible = false
                                }
                            }
                        },
                        onSend = {
                            if (!sending && (draft.isNotBlank() || attachments.isNotEmpty())) {
                                val outgoingText = draft.ifBlank {
                                    "发送了 ${attachments.size} 个附件"
                                }
                                messages += ChatMessage(
                                    author = "你",
                                    text = outgoingText,
                                    isAgent = false,
                                    time = currentTimeText(),
                                    attachments = attachments.toList(),
                                )
                                selectedRecord = updateRecordPreview(
                                    records = records,
                                    current = selectedRecord,
                                    latestText = outgoingText,
                                )
                                saveChatRecords(context, records)
                                val assistantIndex = messages.size
                                messages += ChatMessage(
                                    author = selectedPersona.name,
                                    text = "",
                                    isAgent = true,
                                    time = currentTimeText(),
                                    thinking = "",
                                    showThinking = false,
                                    thinkingExpanded = false,
                                    streaming = true,
                                    mode = selectedMode,
                                )
                                draft = ""
                                attachments.clear()
                                recording = false
                                toolsVisible = false
                                characterExpanded = false
                                saveChatMessages(context, selectedRecord.id, messages)

                                coroutineScope.launch {
                                    streamAssistantMessage(
                                        context = context,
                                        recordId = selectedRecord.id,
                                        messages = messages,
                                        assistantIndex = assistantIndex,
                                        userText = outgoingText,
                                        persona = selectedPersona,
                                        mode = selectedMode,
                                        settings = modelSettings,
                                    )
                                }
                            }
                        },
                    )
                }
            }

            SettingsSheet(
                visible = settingsVisible,
                settings = modelSettings,
                configuredModels = configuredModels,
                sheetMode = settingsSheetMode,
                mode = selectedMode,
                onApply = { nextSettings, nextMode, nextConfiguredModels ->
                    configuredModels.clear()
                    configuredModels.addAll(nextConfiguredModels)
                    modelSettings = nextSettings
                    selectedMode = nextMode
                    saveConfiguredModels(context, configuredModels)
                    saveModelSettings(context, nextSettings)
                    saveChatMode(context, nextMode)
                    settingsVisible = false
                },
                onDismiss = { settingsVisible = false },
            )

            PersonaSheet(
                visible = personaSheetVisible,
                selectedPersona = selectedPersona,
                onSelect = {
                    selectedPersona = it
                    personaSheetVisible = false
                },
                onDismiss = { personaSheetVisible = false },
            )
        }
    }
}

@Composable
private fun ChatScreen(
    selectedPersona: PersonaPreset,
    settings: ModelSettings,
    configuredModels: List<ConfiguredModel>,
    selectedMode: ChatMode,
    sending: Boolean,
    messages: List<ChatMessage>,
    characterExpanded: Boolean,
    recording: Boolean,
    draft: String,
    attachments: List<ComposerAttachment>,
    toolsVisible: Boolean,
    onHistoryOpen: () -> Unit,
    onPersonaClick: () -> Unit,
    onNewChat: () -> Unit,
    onModeChange: (ChatMode) -> Unit,
    onSettingsClick: () -> Unit,
    onModelSelect: (ConfiguredModel) -> Unit,
    onModelConfigure: () -> Unit,
    onCharacterToggle: () -> Unit,
    onToggleThinking: (Int) -> Unit,
    onDraftChange: (String) -> Unit,
    onToolsToggle: () -> Unit,
    onImagePick: () -> Unit,
    onFilePick: () -> Unit,
    onVoiceToggle: () -> Unit,
    onRemoveAttachment: (ComposerAttachment) -> Unit,
    onToolClick: (ToolShortcut) -> Unit,
    onSend: () -> Unit,
) {
    var horizontalDrag by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount -> horizontalDrag += dragAmount },
                    onDragEnd = {
                        if (horizontalDrag < -92f) {
                            onHistoryOpen()
                        }
                        horizontalDrag = 0f
                    },
                    onDragCancel = { horizontalDrag = 0f },
                )
            },
    ) {
        ChatTopBar(
            persona = selectedPersona,
            settings = settings,
            configuredModels = configuredModels,
            mode = selectedMode,
            sending = sending,
            onPersonaClick = onPersonaClick,
            onSettingsClick = onHistoryOpen,
            onNewChat = onNewChat,
            onModeChange = onModeChange,
            onModelSelect = onModelSelect,
            onModelConfigure = onModelConfigure,
        )
        MainConversationArea(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            messages = messages,
            persona = selectedPersona,
            characterExpanded = characterExpanded,
            recording = recording,
            onCharacterToggle = onCharacterToggle,
            onToggleThinking = onToggleThinking,
        )
        ChatComposer(
            draft = draft,
            attachments = attachments,
            toolsVisible = toolsVisible,
            recording = recording,
            sending = sending,
            onDraftChange = onDraftChange,
            onToolsToggle = onToolsToggle,
            onImagePick = onImagePick,
            onFilePick = onFilePick,
            onVoiceToggle = onVoiceToggle,
            onRemoveAttachment = onRemoveAttachment,
            onToolClick = onToolClick,
            onSend = onSend,
        )
    }
}

@Composable
private fun ChatHistoryScreen(
    records: List<ChatRecord>,
    selectedRecord: ChatRecord,
    onAvatarClick: () -> Unit,
    onNewChat: () -> Unit,
    onRecordClick: (ChatRecord) -> Unit,
    onDismiss: () -> Unit,
) {
    var horizontalDrag by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount -> horizontalDrag += dragAmount },
                    onDragEnd = {
                        if (horizontalDrag > 92f) {
                            onDismiss()
                        }
                        horizontalDrag = 0f
                    },
                    onDragCancel = { horizontalDrag = 0f },
                )
            },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                HistoryHeader(
                    onSearchClick = {},
                    onAvatarClick = onAvatarClick,
                )
            }
            item {
                Spacer(modifier = Modifier.height(34.dp))
                HistoryActionRow(
                    icon = Icons.Rounded.Add,
                    title = "新建项目",
                    subtitle = "创建一组任务和上下文",
                    onClick = onNewChat,
                )
                HistoryActionRow(
                    icon = Icons.Rounded.Bolt,
                    title = "Algorithm works",
                    subtitle = "已固定项目",
                    onClick = {},
                    accent = true,
                )
                Spacer(modifier = Modifier.height(26.dp))
                Text(
                    text = "最近",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
            items(records) { record ->
                HistoryRecordRow(
                    record = record,
                    selected = record == selectedRecord,
                    onClick = { onRecordClick(record) },
                )
            }
            item {
                Spacer(modifier = Modifier.height(104.dp))
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 28.dp)
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(999.dp),
            onClick = onNewChat,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                )
                Text(
                    text = "聊天",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun HistoryHeader(
    onSearchClick: () -> Unit,
    onAvatarClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Amaduse",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onSearchClick, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "搜索",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp),
            )
        }
        Surface(
            modifier = Modifier
                .size(46.dp)
                .padding(start = 6.dp),
            color = Color(0xFFB59A62),
            contentColor = Color.White,
            shape = CircleShape,
            onClick = onAvatarClick,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "A",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun HistoryActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    accent: Boolean = false,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (accent) Color(0xFF4D8CFF) else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(30.dp),
            )
            Column {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HistoryRecordRow(
    record: ChatRecord,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(AmaduseMotion.Fast),
        label = "history-row-background",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = record.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            AnimatedVisibility(visible = selected) {
                Text(
                    text = record.subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    persona: PersonaPreset,
    settings: ModelSettings,
    configuredModels: List<ConfiguredModel>,
    mode: ChatMode,
    sending: Boolean,
    onPersonaClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNewChat: () -> Unit,
    onModeChange: (ChatMode) -> Unit,
    onModelSelect: (ConfiguredModel) -> Unit,
    onModelConfigure: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(38.dp)
                .iconGlass(dark = isSystemInDarkTheme()),
        ) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "打开菜单",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        TopModelSelector(
            settings = settings,
            configuredModels = configuredModels,
            sending = sending,
            onModelSelect = onModelSelect,
            onModelConfigure = onModelConfigure,
        )
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(999.dp),
            onClick = onPersonaClick,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = persona.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        TopModeSwitch(
            mode = mode,
            onModeChange = onModeChange,
        )
        IconButton(
            onClick = onNewChat,
            modifier = Modifier
                .size(38.dp)
                .iconGlass(dark = isSystemInDarkTheme()),
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "新会话",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun TopModelSelector(
    settings: ModelSettings,
    configuredModels: List<ConfiguredModel>,
    sending: Boolean,
    onModelSelect: (ConfiguredModel) -> Unit,
    onModelConfigure: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val availableModels = configuredModels.filter { it.apiKey.isNotBlank() }
    val currentConfigured = configuredModels.firstOrNull { it.id == settings.configuredModelId }
    val modelLabel = currentConfigured?.displayName
        ?: settings.provider.name.takeIf { settings.apiKey.isNotBlank() }
        ?: "未配置"

    Box {
        Surface(
            modifier = Modifier
                .width(88.dp)
                .height(38.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(999.dp),
            onClick = { expanded = true },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                StatusDot(active = sending)
                Text(
                    text = modelLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (availableModels.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = "暂无可用模型",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "请先添加 API Key",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onModelConfigure()
                    },
                )
            }
            availableModels.forEach { configured ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = configured.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = configured.model,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onModelSelect(configured)
                        },
                    )
            }
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            text = "自定义",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "跳转到模型配置",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
                onClick = {
                    expanded = false
                    onModelConfigure()
                },
            )
        }
    }
}

@Composable
private fun TopModeSwitch(
    mode: ChatMode,
    onModeChange: (ChatMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .height(38.dp)
            .glassLayer(
                shape = RoundedCornerShape(999.dp),
                dark = isSystemInDarkTheme(),
            )
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopModeSegment(
            text = "快速",
            selected = mode == ChatMode.Fast,
            onClick = { onModeChange(ChatMode.Fast) },
        )
        TopModeSegment(
            text = "思考",
            selected = mode == ChatMode.Thinking,
            onClick = { onModeChange(ChatMode.Thinking) },
        )
    }
}

@Composable
private fun TopModeSegment(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(AmaduseMotion.Fast),
        label = "top-mode-background",
    )
    val content by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(AmaduseMotion.Fast),
        label = "top-mode-content",
    )

    Surface(
        color = background,
        contentColor = content,
        shape = RoundedCornerShape(999.dp),
        onClick = onClick,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun RuntimeControlStrip(
    settings: ModelSettings,
    mode: ChatMode,
    sending: Boolean,
    onModeChange: (ChatMode) -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(999.dp),
            onClick = onSettingsClick,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusDot(active = sending)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${settings.provider.name} · ${settings.model}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (settings.useRemote) "远程 API" else "本地演示流",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Row(
            modifier = Modifier
                .glassLayer(
                    shape = RoundedCornerShape(999.dp),
                    dark = isSystemInDarkTheme(),
                )
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModeSegment(
                mode = ChatMode.Fast,
                selected = mode == ChatMode.Fast,
                onClick = { onModeChange(ChatMode.Fast) },
            )
            ModeSegment(
                mode = ChatMode.Thinking,
                selected = mode == ChatMode.Thinking,
                onClick = { onModeChange(ChatMode.Thinking) },
            )
        }
    }
}

@Composable
private fun ModeSegment(
    mode: ChatMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        animationSpec = tween(AmaduseMotion.Fast),
        label = "mode-background",
    )
    val content by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(AmaduseMotion.Fast),
        label = "mode-content",
    )

    Surface(
        color = background,
        contentColor = content,
        shape = RoundedCornerShape(999.dp),
        onClick = onClick,
    ) {
        Text(
            text = mode.label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun MainConversationArea(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    persona: PersonaPreset,
    characterExpanded: Boolean,
    recording: Boolean,
    onCharacterToggle: () -> Unit,
    onToggleThinking: (Int) -> Unit,
) {
    val stageHeight by animateDpAsState(
        targetValue = if (characterExpanded) 238.dp else 72.dp,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = 260f),
        label = "stage-height",
    )

    Column(
        modifier = modifier.padding(horizontal = AmaduseStyle.ScreenPadding),
    ) {
        AnimatedContent(
            targetState = characterExpanded,
            transitionSpec = {
                fadeIn(tween(AmaduseMotion.Default)) togetherWith
                    fadeOut(tween(AmaduseMotion.Fast)) using
                    SizeTransform(clip = false)
            },
            label = "character-stage-mode",
        ) { expanded ->
            CharacterStage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(stageHeight),
                persona = persona,
                expanded = expanded,
                recording = recording,
                onToggle = onCharacterToggle,
            )
        }
        ChatTranscript(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            messages = messages,
            onExpandCharacter = { if (!characterExpanded) onCharacterToggle() },
            onCollapseCharacter = { if (characterExpanded) onCharacterToggle() },
            onToggleThinking = onToggleThinking,
        )
    }
}

@Composable
private fun CharacterStage(
    modifier: Modifier = Modifier,
    persona: PersonaPreset,
    expanded: Boolean,
    recording: Boolean,
    onToggle: () -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val transition = rememberInfiniteTransition(label = "character-motion")
    val breath by transition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "character-breath",
    )
    val scanAlpha by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scan-alpha",
    )
    val avatarScale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.58f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 280f),
        label = "avatar-scale",
    )
    val shape = RoundedCornerShape(if (expanded) AmaduseStyle.PanelRadius else 999.dp)

    Box(
        modifier = modifier
            .padding(bottom = if (expanded) 12.dp else 8.dp)
            .glassLayer(shape = shape, dark = dark)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (dark) {
                        listOf(Color(0xFF171719), Color(0xFF080809))
                    } else {
                        listOf(Color.White, Color(0xFFEDEDEA))
                    },
                ),
                shape = shape,
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onToggle,
            )
            .padding(horizontal = if (expanded) 18.dp else 12.dp, vertical = if (expanded) 14.dp else 8.dp),
    ) {
        Column(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = scanAlpha },
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(if (expanded) 8 else 2) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(if (expanded) 0.48f else 0.2f),
                contentAlignment = Alignment.Center,
            ) {
                AgentSilhouette(
                    modifier = Modifier
                        .fillMaxHeight(if (expanded) 0.92f else 0.78f)
                        .widthIn(min = 52.dp, max = if (expanded) 170.dp else 60.dp)
                        .scale(breath * avatarScale),
                    compact = !expanded,
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(AmaduseMotion.Default)) + slideInVertically { it / 4 },
                exit = fadeOut(tween(AmaduseMotion.Fast)) + slideOutVertically { it / 4 },
                modifier = Modifier.weight(0.52f),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatusPill(
                        text = if (recording) "正在听取语音" else "在线 · 前端原型",
                        active = recording,
                    )
                    Text(
                        text = persona.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = persona.tone,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CompactTag("文字")
                        CompactTag("语音")
                        CompactTag("相机")
                        CompactTag("PC")
                    }
                }
            }
            AnimatedVisibility(
                visible = !expanded,
                enter = fadeIn(tween(AmaduseMotion.Default)),
                exit = fadeOut(tween(AmaduseMotion.Fast)),
                modifier = Modifier.weight(0.8f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = persona.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (recording) "Listening..." else "轻点展开人物区域",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    StatusDot(active = recording)
                }
            }
        }
    }
}

@Composable
private fun AgentSilhouette(
    modifier: Modifier = Modifier,
    compact: Boolean,
) {
    val dark = isSystemInDarkTheme()
    val bodyColor = if (dark) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 34.dp else 72.dp)
                .clip(CircleShape)
                .background(bodyColor),
        )
        Spacer(modifier = Modifier.height(if (compact) 6.dp else 14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(if (compact) 0.72f else 0.84f)
                .weight(1f)
                .clip(
                    RoundedCornerShape(
                        topStart = if (compact) 20.dp else 48.dp,
                        topEnd = if (compact) 20.dp else 48.dp,
                        bottomStart = if (compact) 8.dp else 16.dp,
                        bottomEnd = if (compact) 8.dp else 16.dp,
                    ),
                )
                .background(bodyColor),
        )
    }
}

@Composable
private fun ChatTranscript(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    onExpandCharacter: () -> Unit,
    onCollapseCharacter: () -> Unit,
    onToggleThinking: (Int) -> Unit,
) {
    val listState = rememberLazyListState()
    var dragAmount by remember { mutableFloatStateOf(0f) }
    val hasStreamingMessage = messages.any { it.streaming }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragDelta -> dragAmount += dragDelta },
                    onDragEnd = {
                        if (dragAmount < -70f) {
                            onCollapseCharacter()
                        } else if (dragAmount > 90f && listState.firstVisibleItemIndex == 0) {
                            onExpandCharacter()
                        }
                        dragAmount = 0f
                    },
                    onDragCancel = { dragAmount = 0f },
                )
            },
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ConversationDateDivider(text = "今天")
        }
        itemsIndexed(messages) { index, message ->
            MessageRow(
                message = message,
                onToggleThinking = { onToggleThinking(index) },
            )
        }
        if (hasStreamingMessage) {
            item {
                TypingPreview()
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ConversationDateDivider(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun MessageRow(
    message: ChatMessage,
    onToggleThinking: () -> Unit,
) {
    if (message.isAgent) {
        AgentMessage(
            message = message,
            onToggleThinking = onToggleThinking,
        )
    } else {
        UserMessage(message)
    }
}

@Composable
private fun AgentMessage(
    message: ChatMessage,
    onToggleThinking: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(AmaduseMotion.Default)),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "A",
                color = MaterialTheme.colorScheme.background,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.author,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message.time,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            ThinkingTraceCard(
                thinking = message.thinking,
                visible = message.showThinking,
                expanded = message.thinkingExpanded,
                mode = message.mode,
                streaming = message.streaming,
                onToggle = onToggleThinking,
            )
            if (message.text.isNotBlank()) {
                Text(
                    text = message.text,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                )
            } else if (message.streaming) {
                StreamingLinePlaceholder()
            }
            message.toolPreview?.let {
                ToolConfirmationCard(tool = it)
            }
            if (message.attachments.isNotEmpty()) {
                AttachmentRow(attachments = message.attachments)
            }
        }
    }
}

@Composable
private fun ThinkingTraceCard(
    thinking: String,
    visible: Boolean,
    expanded: Boolean,
    mode: ChatMode,
    streaming: Boolean,
    onToggle: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible && thinking.isNotBlank(),
        enter = fadeIn(tween(AmaduseMotion.Default)) + slideInVertically { it / 4 },
        exit = fadeOut(tween(AmaduseMotion.Fast)) + slideOutVertically { it / 4 },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassLayer(
                    shape = RoundedCornerShape(18.dp),
                    dark = isSystemInDarkTheme(),
                )
                .clickable(onClick = onToggle)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusDot(active = streaming)
                Text(
                    text = "思考过程",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (expanded) "收起" else "展开",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(AmaduseMotion.Default)) + slideInVertically { it / 6 },
                exit = fadeOut(tween(AmaduseMotion.Fast)) + slideOutVertically { it / 6 },
            ) {
                AnimatedContent(
                    targetState = thinking,
                    transitionSpec = {
                        fadeIn(tween(AmaduseMotion.Default)) togetherWith fadeOut(tween(AmaduseMotion.Fast))
                    },
                    label = "thinking-content",
                ) { text ->
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamingLinePlaceholder() {
    Row(
        modifier = Modifier.padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusDot(active = true)
        Text(
            text = "正在生成回复...",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun UserMessage(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 308.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(
                topStart = AmaduseStyle.BubbleRadius,
                topEnd = AmaduseStyle.BubbleRadius,
                bottomStart = AmaduseStyle.BubbleRadius,
                bottomEnd = 6.dp,
            ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 23.sp,
                )
                if (message.attachments.isNotEmpty()) {
                    AttachmentRow(attachments = message.attachments)
                }
            }
        }
        Text(
            text = message.time,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(end = 8.dp),
        )
    }
}

@Composable
private fun ToolConfirmationCard(tool: ToolPreview) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassLayer(
                shape = RoundedCornerShape(18.dp),
                dark = isSystemInDarkTheme(),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = tool.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = tool.detail,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
        )
        Text(
            text = tool.risk,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionPill(
                text = "确认",
                selected = true,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            ActionPill(
                text = "取消",
                selected = false,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AttachmentRow(attachments: List<ComposerAttachment>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        attachments.forEach { attachment ->
            AttachmentLabel(attachment = attachment)
        }
    }
}

@Composable
private fun AttachmentLabel(attachment: ComposerAttachment) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = if (attachment.kind == AttachmentKind.Image) Icons.Rounded.Image else Icons.Rounded.AttachFile,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = attachment.label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TypingPreview() {
    val transition = rememberInfiniteTransition(label = "typing-preview")
    val dotScale by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(640),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "typing-dot",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(5.dp + (index.dp / 2))
                        .scale(dotScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
        }
    }
}

@Composable
private fun ChatComposer(
    draft: String,
    attachments: List<ComposerAttachment>,
    toolsVisible: Boolean,
    recording: Boolean,
    sending: Boolean,
    onDraftChange: (String) -> Unit,
    onToolsToggle: () -> Unit,
    onImagePick: () -> Unit,
    onFilePick: () -> Unit,
    onVoiceToggle: () -> Unit,
    onRemoveAttachment: (ComposerAttachment) -> Unit,
    onToolClick: (ToolShortcut) -> Unit,
    onSend: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val canSend = !sending && (draft.isNotBlank() || attachments.isNotEmpty())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnimatedVisibility(
            visible = toolsVisible,
            enter = fadeIn(tween(AmaduseMotion.Default)) + slideInVertically { it / 3 },
            exit = fadeOut(tween(AmaduseMotion.Fast)) + slideOutVertically { it / 3 },
        ) {
            ToolTray(onToolClick = onToolClick)
        }

        AnimatedVisibility(
            visible = attachments.isNotEmpty(),
            enter = fadeIn(tween(AmaduseMotion.Default)) + slideInVertically { it / 4 },
            exit = fadeOut(tween(AmaduseMotion.Fast)) + slideOutVertically { it / 4 },
        ) {
            AttachmentComposerStrip(
                attachments = attachments,
                onRemoveAttachment = onRemoveAttachment,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassLayer(
                    shape = RoundedCornerShape(AmaduseStyle.ControlRadius),
                    dark = isSystemInDarkTheme(),
                )
                .animateContentSize(animationSpec = tween(AmaduseMotion.Default))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ComposerIconButton(
                icon = if (toolsVisible) Icons.Rounded.Close else Icons.Rounded.Add,
                contentDescription = if (toolsVisible) "关闭工具" else "打开工具",
                active = toolsVisible,
                onClick = onToolsToggle,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 136.dp)
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send,
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (canSend) {
                                onSend()
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        },
                    ),
                    minLines = 1,
                    maxLines = 6,
                    interactionSource = remember { MutableInteractionSource() },
                    decorationBox = { innerTextField ->
                        if (draft.isBlank()) {
                            Text(
                                text = when {
                                    sending -> "Amaduse 正在输出..."
                                    recording -> "正在听..."
                                    else -> "Message Amaduse"
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp,
                            )
                        }
                        innerTextField()
                    },
                )
            }
            AnimatedContent(
                targetState = when {
                    sending -> "streaming"
                    canSend -> "send"
                    else -> "voice"
                },
                transitionSpec = {
                    fadeIn(tween(AmaduseMotion.Fast)) + scaleIn(initialScale = 0.84f) togetherWith
                        fadeOut(tween(AmaduseMotion.Fast)) + scaleOut(targetScale = 0.84f)
                },
                label = "composer-action",
            ) { state ->
                if (state == "send") {
                    IconButton(
                        onClick = {
                            onSend()
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "发送",
                        )
                    }
                } else if (state == "streaming") {
                    IconButton(
                        onClick = {},
                        enabled = false,
                        colors = IconButtonDefaults.iconButtonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.46f),
                        ),
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "输出中",
                        )
                    }
                } else {
                    ComposerIconButton(
                        icon = Icons.Rounded.Mic,
                        contentDescription = "语音输入",
                        active = recording,
                        onClick = {
                            onVoiceToggle()
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolTray(onToolClick: (ToolShortcut) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .glassLayer(
                shape = RoundedCornerShape(22.dp),
                dark = isSystemInDarkTheme(),
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        toolShortcuts.forEach { shortcut ->
            ToolShortcutChip(
                shortcut = shortcut,
                onClick = { onToolClick(shortcut) },
            )
        }
    }
}

@Composable
private fun ToolShortcutChip(
    shortcut: ToolShortcut,
    onClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.64f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(18.dp),
        onClick = onClick,
        modifier = Modifier.widthIn(min = 108.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = shortcut.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Column {
                Text(
                    text = shortcut.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = shortcut.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AttachmentComposerStrip(
    attachments: List<ComposerAttachment>,
    onRemoveAttachment: (ComposerAttachment) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .glassLayer(
                shape = RoundedCornerShape(18.dp),
                dark = isSystemInDarkTheme(),
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        attachments.forEach { attachment ->
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = attachment.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (attachment.kind == AttachmentKind.Image) {
                            Icons.Rounded.Image
                        } else {
                            Icons.Rounded.AttachFile
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { onRemoveAttachment(attachment) },
                        modifier = Modifier.size(22.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "移除附件",
                            modifier = Modifier.size(14.dp),
                        )
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color.Transparent,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
    }
}

@Composable
private fun ComposerIconButton(
    icon: ImageVector,
    contentDescription: String,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    val activeScale by animateFloatAsState(
        targetValue = if (active) 1.08f else 1f,
        animationSpec = tween(AmaduseMotion.Fast),
        label = "composer-icon-scale",
    )
    val tint by animateColorAsState(
        targetValue = if (active) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(AmaduseMotion.Fast),
        label = "composer-icon-tint",
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .scale(activeScale)
            .iconGlass(dark = isSystemInDarkTheme()),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun SettingsSheet(
    visible: Boolean,
    settings: ModelSettings,
    configuredModels: List<ConfiguredModel>,
    sheetMode: SettingsSheetMode,
    mode: ChatMode,
    onApply: (ModelSettings, ChatMode, List<ConfiguredModel>) -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(AmaduseMotion.Default)),
        exit = fadeOut(tween(AmaduseMotion.Fast)),
    ) {
        val configured = remember(visible, configuredModels) {
            mutableStateListOf<ConfiguredModel>().apply { addAll(configuredModels) }
        }
        val initialConfigured = configuredModels.firstOrNull { it.id == settings.configuredModelId }
        var editingModelId by remember(visible, settings) { mutableStateOf(initialConfigured?.id) }
        var provider by remember(visible, settings) { mutableStateOf(initialConfigured?.provider ?: settings.provider) }
        var displayName by remember(visible, settings) {
            mutableStateOf(initialConfigured?.displayName ?: settings.provider.name)
        }
        var model by remember(visible, settings) { mutableStateOf(initialConfigured?.model ?: settings.model) }
        var baseUrl by remember(visible, settings) { mutableStateOf(initialConfigured?.baseUrl ?: settings.baseUrl) }
        var apiKey by remember(visible, settings) { mutableStateOf(initialConfigured?.apiKey ?: settings.apiKey) }
        var selectedMode by remember(visible, mode) { mutableStateOf(mode) }
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .navigationBarsPadding()
                    .glassLayer(
                        shape = RoundedCornerShape(28.dp),
                        dark = isSystemInDarkTheme(),
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {},
                    )
                    .padding(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (sheetMode == SettingsSheetMode.ModelOnly) "模型配置" else "自定义 Amaduse",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "关闭",
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (sheetMode == SettingsSheetMode.Full) {
                        SettingsSection(title = "") {
                            SettingsMenuRow(
                                icon = Icons.Rounded.Person,
                                title = "个性化",
                                value = "人格、语调、称呼",
                                onClick = {},
                            )
                        }

                        SettingsSection(title = "账户") {
                            SettingsMenuRow(
                                icon = Icons.Rounded.Person,
                                title = "用户",
                                value = "Local Agent",
                                onClick = {},
                            )
                            SettingsMenuRow(
                                icon = Icons.Rounded.Bolt,
                                title = "订阅",
                                value = "Amaduse Lab",
                                onClick = {},
                            )
                            SettingsMenuRow(
                                icon = Icons.Rounded.Computer,
                                title = "恢复购买",
                                value = "",
                                onClick = {},
                            )
                        }
                    }

                    SettingsSection(title = "模型配置服务") {
                        val configuredWithKey = configured.filter { it.apiKey.isNotBlank() }
                        if (configuredWithKey.isEmpty()) {
                            Text(
                                text = "还没有配置 API Key 的模型。添加后会出现在顶部模型下拉栏。",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp,
                            )
                        } else {
                            configuredWithKey.forEach { item ->
                                ConfiguredModelRow(
                                    model = item,
                                    selected = item.id == editingModelId,
                                    onEdit = {
                                        editingModelId = item.id
                                        provider = item.provider
                                        displayName = item.displayName
                                        model = item.model
                                        baseUrl = item.baseUrl
                                        apiKey = item.apiKey
                                    },
                                    onDelete = {
                                        configured.removeAll { it.id == item.id }
                                        if (editingModelId == item.id) {
                                            editingModelId = null
                                            provider = modelProviderPresets.first { it.name == "自定义" }
                                            displayName = "自定义"
                                            model = provider.defaultModel
                                            baseUrl = provider.baseUrl
                                            apiKey = ""
                                        }
                                    },
                                )
                            }
                        }
                        ActionPill(
                            text = "添加模型",
                            selected = false,
                            onClick = {
                                editingModelId = null
                                provider = modelProviderPresets.first { it.name == "自定义" }
                                displayName = "自定义"
                                model = provider.defaultModel
                                baseUrl = provider.baseUrl
                                apiKey = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Add,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            modelProviderPresets.filter { it.name != "演示模型" }.forEach { preset ->
                                ProviderChip(
                                    preset = preset,
                                    selected = preset == provider,
                                    onClick = {
                                        provider = preset
                                        model = preset.defaultModel
                                        baseUrl = preset.baseUrl
                                        if (displayName.isBlank() || displayName == "自定义" || displayName == settings.provider.name) {
                                            displayName = preset.name
                                        }
                                    },
                                )
                            }
                        }
                    }

                    SettingsSection(title = "自定义模型参数配置") {
                        Text(
                            text = provider.note,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                        )
                        SettingsTextInput(
                            label = "显示名称",
                            value = displayName,
                            onValueChange = { displayName = it },
                            placeholder = provider.name,
                        )
                        SettingsTextInput(
                            label = "Base URL",
                            value = baseUrl,
                            onValueChange = { baseUrl = it },
                        )
                        SettingsTextInput(
                            label = "Model",
                            value = model,
                            onValueChange = { model = it },
                        )
                        SettingsTextInput(
                            label = "API Key",
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            placeholder = "sk-...",
                            secret = true,
                        )
                    }

                    if (sheetMode == SettingsSheetMode.Full) {
                        SettingsSection(title = "回答模式") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ModeCard(
                                    mode = ChatMode.Fast,
                                    selected = selectedMode == ChatMode.Fast,
                                    onClick = { selectedMode = ChatMode.Fast },
                                    modifier = Modifier.weight(1f),
                                )
                                ModeCard(
                                    mode = ChatMode.Thinking,
                                    selected = selectedMode == ChatMode.Thinking,
                                    onClick = { selectedMode = ChatMode.Thinking },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        SettingsSection(title = "主题") {
                            SettingsMenuRow(
                                icon = Icons.Rounded.Settings,
                                title = "外观",
                                value = "系统",
                                onClick = {},
                            )
                            SettingsMenuRow(
                                icon = Icons.Rounded.Check,
                                title = "重点色",
                                value = "默认",
                                onClick = {},
                            )
                        }

                        SettingsSection(title = "应用设置") {
                            SettingsMenuRow(
                                icon = Icons.Rounded.Settings,
                                title = "常规",
                                value = "",
                                onClick = {},
                            )
                            SettingsMenuRow(
                                icon = Icons.Rounded.Mic,
                                title = "声音",
                                value = "",
                                onClick = {},
                            )
                            SettingsMenuRow(
                                icon = Icons.Rounded.Bolt,
                                title = "安全与安保",
                                value = "",
                                onClick = {},
                            )
                            SettingsMenuRow(
                                icon = Icons.Rounded.Description,
                                title = "数据控制",
                                value = "",
                                onClick = {},
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ActionPill(
                        text = "取消",
                        selected = false,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    ActionPill(
                        text = "保存",
                        selected = true,
                        onClick = {
                            val savedModel = ConfiguredModel(
                                id = editingModelId ?: UUID.randomUUID().toString(),
                                displayName = displayName.ifBlank { provider.name },
                                provider = provider,
                                model = model.ifBlank { provider.defaultModel },
                                baseUrl = baseUrl.ifBlank { provider.baseUrl },
                                apiKey = apiKey,
                            )
                            val existingIndex = configured.indexOfFirst { it.id == savedModel.id }
                            if (existingIndex >= 0) {
                                configured[existingIndex] = savedModel
                            } else {
                                configured.add(0, savedModel)
                            }
                            onApply(
                                savedModel.toModelSettings(),
                                selectedMode,
                                configured.toList(),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfiguredModelRow(
    model: ConfiguredModel,
    selected: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${model.provider.name} · ${model.model}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionPill(
                text = "编辑",
                selected = selected,
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Edit,
            )
            ActionPill(
                text = "删除",
                selected = false,
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Close,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassLayer(
                    shape = RoundedCornerShape(20.dp),
                    dark = isSystemInDarkTheme(),
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingsMenuRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (value.isNotBlank()) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
            Text(
                text = "›",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Light,
            )
        }
    }
}

@Composable
private fun ProviderChip(
    preset: ModelProviderPreset,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(999.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = preset.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ModeCard(
    mode: ChatMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(18.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = mode.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = mode.caption,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.76f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun SettingsTextInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    secret: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                singleLine = true,
                visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
                decorationBox = { innerTextField ->
                    if (value.isBlank() && placeholder.isNotBlank()) {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                },
            )
        }
    }
}

@Composable
private fun PersonaSheet(
    visible: Boolean,
    selectedPersona: PersonaPreset,
    onSelect: (PersonaPreset) -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(AmaduseMotion.Default)),
        exit = fadeOut(tween(AmaduseMotion.Fast)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .navigationBarsPadding()
                    .glassLayer(
                        shape = RoundedCornerShape(28.dp),
                        dark = isSystemInDarkTheme(),
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {},
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "选择人格",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "关闭",
                        )
                    }
                }
                personaPresets.forEach { persona ->
                    PersonaRow(
                        persona = persona,
                        selected = persona == selectedPersona,
                        onClick = { onSelect(persona) },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionPill(
                        text = "新建人格",
                        selected = false,
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Person,
                    )
                    ActionPill(
                        text = "设置",
                        selected = false,
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Settings,
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonaRow(
    persona: PersonaPreset,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (selected) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(18.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (selected) 0.92f else 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = persona.name.take(1),
                    color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = persona.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = persona.subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            AnimatedVisibility(visible = selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ActionPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier.height(40.dp),
        color = background,
        contentColor = content,
        shape = RoundedCornerShape(999.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CompactTag(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
    )
}

@Composable
private fun StatusPill(
    text: String,
    active: Boolean,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StatusDot(active = active)
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusDot(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "status-dot")
    val alpha by transition.animateFloat(
        initialValue = if (active) 0.38f else 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (active) 520 else 1300),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "status-dot-alpha",
    )
    val dotColor = if (active) Color(0xFF35D07F) else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(dotColor.copy(alpha = alpha)),
    )
}

private suspend fun streamAssistantMessage(
    context: Context,
    recordId: String,
    messages: MutableList<ChatMessage>,
    assistantIndex: Int,
    userText: String,
    persona: PersonaPreset,
    mode: ChatMode,
    settings: ModelSettings,
) {
    fun update(transform: (ChatMessage) -> ChatMessage) {
        if (assistantIndex in messages.indices) {
            messages[assistantIndex] = transform(messages[assistantIndex])
        }
    }

    val requestHistory = messages.take(assistantIndex)

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
            update { it.copy(text = it.text + chunk) }
        },
    )

    if (error != null) {
        splitForStreaming("请求失败：$error").forEach { chunk ->
            update { it.copy(text = it.text + chunk) }
            delay(18)
        }
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

        val payload = JSONObject()
            .put("model", settings.model.ifBlank { settings.provider.defaultModel })
            .put("stream", true)
            .put("messages", payloadMessages)

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
                    val reasoning = cleanDisplayText(cleanStreamChunk(delta.cleanString("reasoning_content")))
                        .ifBlank { cleanDisplayText(cleanStreamChunk(delta.cleanString("reasoning"))) }
                    if (reasoning.isNotBlank()) {
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
    return buildString {
        append("你是 Amaduse Android App 中的个性化智能 Agent。")
        append("当前人格：${persona.name}。语气：${persona.tone}。")
        append("回答应自然、简洁、具备人格感。")
        if (mode == ChatMode.Fast) {
            append("当前为快速模式：直接回答，减少铺垫。")
        } else {
            append("当前为思考模式：可以输出简短 reasoning_content 或在回答前给出高层次分析摘要。")
        }
        append("涉及闹钟、短信、消息回复、电脑任务等真实操作时，必须先说明需要用户确认。")
    }
}

private fun splitForStreaming(text: String): List<String> {
    if (text.isBlank()) {
        return emptyList()
    }

    val chunks = mutableListOf<String>()
    var index = 0
    while (index < text.length) {
        val next = (index + 3).coerceAtMost(text.length)
        chunks += text.substring(index, next)
        index = next
    }
    return chunks
}

private fun JSONObject?.cleanString(key: String): String {
    if (this == null || !has(key) || isNull(key)) {
        return ""
    }
    return optString(key, "")
}

private fun cleanStreamChunk(value: String): String {
    val trimmed = value.trim()
    return when {
        trimmed.isBlank() -> ""
        trimmed.equals("null", ignoreCase = true) -> ""
        trimmed.allNullTokens() -> ""
        else -> value
    }
}

private fun cleanDisplayText(value: String): String {
    return value
        .replace(Regex("(?i)(null){2,}"), "")
        .let { if (it.trim().equals("null", ignoreCase = true)) "" else it }
}

private fun String.allNullTokens(): Boolean {
    if (isBlank() || length % 4 != 0) {
        return false
    }
    return chunked(4).all { it.equals("null", ignoreCase = true) }
}

private fun defaultModelSettings(): ModelSettings {
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

private fun ConfiguredModel.toModelSettings(): ModelSettings {
    return ModelSettings(
        configuredModelId = id,
        provider = provider,
        model = model,
        baseUrl = baseUrl,
        apiKey = apiKey,
        useRemote = apiKey.isNotBlank(),
    )
}

private fun loadConfiguredModels(context: Context): List<ConfiguredModel> {
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

private fun saveConfiguredModels(context: Context, configuredModels: List<ConfiguredModel>) {
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

private fun loadModelSettings(context: Context, configuredModels: List<ConfiguredModel>): ModelSettings {
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

private fun saveModelSettings(context: Context, settings: ModelSettings) {
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

private fun loadChatMode(context: Context): ChatMode {
    val value = context.getSharedPreferences("amaduse_settings", Context.MODE_PRIVATE)
        .getString("chat_mode", ChatMode.Fast.name)
    return runCatching { ChatMode.valueOf(value ?: ChatMode.Fast.name) }.getOrDefault(ChatMode.Fast)
}

private fun saveChatMode(context: Context, mode: ChatMode) {
    context.getSharedPreferences("amaduse_settings", Context.MODE_PRIVATE)
        .edit()
        .putString("chat_mode", mode.name)
        .apply()
}

private fun loadChatRecords(context: Context): List<ChatRecord> {
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

private fun saveChatRecords(context: Context, records: List<ChatRecord>) {
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

private fun loadChatMessages(context: Context, recordId: String): List<ChatMessage> {
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

private fun saveChatMessages(context: Context, recordId: String, messages: List<ChatMessage>) {
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
        mode = runCatching {
            ChatMode.valueOf(json.optString("mode"))
        }.getOrDefault(ChatMode.Fast),
    )
}

private fun updateRecordPreview(
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

private fun createNewChatRecord(): ChatRecord {
    return ChatRecord(
        title = "新聊天",
        subtitle = currentTimeText(),
        active = true,
        id = UUID.randomUUID().toString(),
    )
}

private fun currentTimeText(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
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

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AmaduseAppPreview() {
    AmaduseTheme {
        AmaduseApp()
    }
}
