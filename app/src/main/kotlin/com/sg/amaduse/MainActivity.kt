package com.sg.amaduse

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material.icons.rounded.ContentCopy
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Canvas
import com.sg.amaduse.agent.audio.AssistantSpeechConfig
import com.sg.amaduse.agent.audio.DEFAULT_VOICE_REFERENCE_SOURCE
import com.sg.amaduse.agent.audio.SpeechOutputCoordinator
import com.sg.amaduse.agent.audio.SpeechTranslationConfig
import com.sg.amaduse.agent.audio.SiliconFlowVoiceService
import com.sg.amaduse.agent.audio.VoiceSettings
import com.sg.amaduse.agent.model.ChatCompletionMode
import com.sg.amaduse.agent.model.ChatCompletionModeAdapter
import com.sg.amaduse.agent.model.ChatCompletionModeConfig
import com.sg.amaduse.agent.persona.KURISU_REFERENCE_TEXT
import com.sg.amaduse.agent.persona.PersonaPreset
import com.sg.amaduse.agent.persona.PersonaPromptBuilder
import com.sg.amaduse.agent.persona.PromptMode
import com.sg.amaduse.agent.persona.PromptRuntimeContext
import com.sg.amaduse.agent.persona.personaPresets
import com.sg.amaduse.agent.tools.AddMemoTool
import com.sg.amaduse.agent.tools.AgentToolContext
import com.sg.amaduse.agent.tools.CreateLocalFileTool
import com.sg.amaduse.agent.tools.SetAlarmTool
import com.sg.amaduse.agent.tools.TestEmotionTool
import com.sg.amaduse.agent.tools.TestVoiceTool
import com.sg.amaduse.agent.tools.ToolRegistry
import com.sg.amaduse.agent.tools.WebSearchTool
import com.sg.amaduse.ui.theme.AmaduseMotion
import com.sg.amaduse.ui.theme.AmaduseStyle
import com.sg.amaduse.ui.theme.AmaduseTheme
import com.sg.amaduse.ui.theme.glassLayer
import com.sg.amaduse.ui.theme.iconGlass
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
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
import java.util.TimeZone
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
    var voiceSettings by remember { mutableStateOf(loadVoiceSettings(context)) }
    var personaSheetVisible by remember { mutableStateOf(false) }
    var settingsVisible by remember { mutableStateOf(false) }
    var settingsSheetMode by remember { mutableStateOf(SettingsSheetMode.Full) }
    var newChatConfirmVisible by remember { mutableStateOf(false) }
    var deleteConfirmRecord by remember { mutableStateOf<ChatRecord?>(null) }
    var toolsVisible by remember { mutableStateOf(false) }
    var characterExpanded by remember { mutableStateOf(false) }
    var recording by remember { mutableStateOf(false) }
    var live2dCanvasReady by remember { mutableStateOf(false) }
    var activeResponseJob by remember { mutableStateOf<Job?>(null) }
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
    val pendingCalendarPermission = remember { mutableStateOf<CompletableDeferred<Boolean>?>(null) }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants[Manifest.permission.READ_CALENDAR] == true &&
            grants[Manifest.permission.WRITE_CALENDAR] == true
        pendingCalendarPermission.value?.complete(granted)
        pendingCalendarPermission.value = null
    }
    val requestCalendarPermissions: suspend () -> Boolean = {
        val alreadyGranted = context.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
            context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            true
        } else {
            withContext(Dispatchers.Main) {
                pendingCalendarPermission.value?.let { existing ->
                    return@withContext existing.await()
                }
                val request = CompletableDeferred<Boolean>()
                pendingCalendarPermission.value = request
                calendarPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR,
                    ),
                )
                request.await()
            }
        }
    }
    val sending = messages.any { it.streaming }

    val live2dWebView = remember(context) {
        WebView(context).apply {
            setBackgroundColor(android.graphics.Color.rgb(240, 245, 246))
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    Log.d(
                        "AmaduseLive2D",
                        "${consoleMessage.message()} (${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})",
                    )
                    return true
                }
            }
            webViewClient = WebViewClient()
            addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun onCanvasReady() {
                        post {
                            live2dCanvasReady = true
                        }
                    }
                },
                "AmaduseAndroid",
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            loadUrl("file:///android_asset/live2d_viewer.html")
        }
    }

    // Register agent tools once
    val agentToolContext = remember(context, voiceSettings, modelSettings, live2dWebView) {
        val ctx = AgentToolContext(
            appContext = context,
            voiceSettings = voiceSettings,
            modelSettings = modelSettings,
            live2dWebView = live2dWebView,
            requestCalendarPermissions = requestCalendarPermissions,
        )
        ToolRegistry.register(TestVoiceTool())
        ToolRegistry.register(TestEmotionTool())
        ToolRegistry.register(SetAlarmTool())
        ToolRegistry.register(AddMemoTool())
        ToolRegistry.register(WebSearchTool())
        ToolRegistry.register(CreateLocalFileTool())
        ctx
    }

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
                            android.util.Log.d("Amaduse", "Avatar clicked! settingsVisible=$settingsVisible")
                            settingsSheetMode = SettingsSheetMode.Full
                            settingsVisible = true
                            android.util.Log.d("Amaduse", "After set: settingsVisible=$settingsVisible")
                        },
                        onNewChat = {
                            newChatConfirmVisible = true
                        },
                        onRecordClick = { record ->
                            selectedRecord = record
                            messages.clear()
                            messages += loadChatMessages(context, record.id)
                            appScreen = AppScreen.Chat
                        },
                        onDeleteRecord = { record ->
                            deleteConfirmRecord = record
                        },
                        onDismiss = { appScreen = AppScreen.Chat },
                    )
                } else {
                    ChatScreen(
                        live2dWebView = live2dWebView,
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
                            newChatConfirmVisible = true
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
                                        "备忘" -> "帮我创建一个日历任务"
                                        "电脑" -> "帮我把这个任务发送到电脑"
                                        else -> draft
                                    }
                                    toolsVisible = false
                                }
                            }
                        },
                        onStop = {
                            activeResponseJob?.cancel()
                            activeResponseJob = null
                            SiliconFlowVoiceService.stopPlayback()
                            val streamingIndex = messages.indexOfLast { it.streaming }
                            if (streamingIndex >= 0) {
                                val current = messages[streamingIndex]
                                messages[streamingIndex] = current.copy(
                                    text = current.text.ifBlank { "已中断。" },
                                    streaming = false,
                                    activeToolName = null,
                                    thinkingExpanded = false,
                                    showThinking = current.thinking.isNotBlank(),
                                )
                                saveChatMessages(context, selectedRecord.id, messages)
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

                                lateinit var responseJob: Job
                                responseJob = coroutineScope.launch {
                                    try {
                                        streamAssistantMessage(
                                            context = context,
                                            recordId = selectedRecord.id,
                                            messages = messages,
                                            assistantIndex = assistantIndex,
                                            userText = outgoingText,
                                            persona = selectedPersona,
                                            mode = selectedMode,
                                            settings = modelSettings,
                                            voiceSettings = voiceSettings,
                                            live2dWebView = live2dWebView,
                                            agentToolContext = agentToolContext,
                                        )
                                    } finally {
                                        if (activeResponseJob === responseJob) {
                                            activeResponseJob = null
                                        }
                                    }
                                }
                                activeResponseJob = responseJob
                            }
                        },
                    )
                }
            }

            SettingsSheet(
                visible = settingsVisible,
                settings = modelSettings,
                voiceSettings = voiceSettings,
                configuredModels = configuredModels,
                sheetMode = settingsSheetMode,
                mode = selectedMode,
                onApply = { nextSettings, nextVoiceSettings, nextMode, nextConfiguredModels ->
                    configuredModels.clear()
                    configuredModels.addAll(nextConfiguredModels)
                    modelSettings = nextSettings
                    voiceSettings = nextVoiceSettings
                    selectedMode = nextMode
                    saveConfiguredModels(context, configuredModels)
                    saveModelSettings(context, nextSettings)
                    saveVoiceSettings(context, nextVoiceSettings)
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

            NewChatConfirmSheet(
                visible = newChatConfirmVisible,
                onConfirm = {
                    val newRecord = createNewChatRecord()
                    records.add(0, newRecord)
                    selectedRecord = newRecord
                    messages.clear()
                    saveChatRecords(context, records)
                    saveChatMessages(context, newRecord.id, messages)
                    characterExpanded = false
                    appScreen = AppScreen.Chat
                    newChatConfirmVisible = false
                },
                onDismiss = { newChatConfirmVisible = false },
            )

            DeleteConfirmSheet(
                visible = deleteConfirmRecord != null,
                recordTitle = deleteConfirmRecord?.title ?: "",
                onConfirm = {
                    deleteConfirmRecord?.let { record ->
                        records.remove(record)
                        saveChatRecords(context, records)
                        if (selectedRecord == record && records.isNotEmpty()) {
                            selectedRecord = records.first()
                            messages.clear()
                            messages += loadChatMessages(context, selectedRecord.id)
                        }
                    }
                    deleteConfirmRecord = null
                },
                onDismiss = { deleteConfirmRecord = null },
            )

            AnimatedVisibility(
                visible = !live2dCanvasReady,
                exit = fadeOut(tween(AmaduseMotion.Slow)),
                modifier = Modifier.fillMaxSize(),
            ) {
                StartupLoadingOverlay()
            }
        }
    }
}

@Composable
private fun StartupLoadingOverlay() {
    val dark = isSystemInDarkTheme()
    val transition = rememberInfiniteTransition(label = "startup-loading")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1400, easing = LinearEasing)),
        label = "startup-ring",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(980, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "startup-pulse",
    )
    val scanAlpha by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.34f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "startup-scan",
    )
    val background = if (dark) {
        Brush.verticalGradient(
            listOf(Color(0xFF080809), Color(0xFF121416), Color(0xFF09090B)),
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFF7F7F5), Color(0xFFE8ECEF), Color(0xFFF2F5F4)),
        )
    }
    val ink = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = scanAlpha },
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(14) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ink.copy(alpha = 0.12f)),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .glassLayer(shape = CircleShape, dark = dark),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier = Modifier
                        .size(86.dp)
                        .rotate(rotation),
                ) {
                    val radius = size.minDimension / 2f
                    drawCircle(
                        color = ink.copy(alpha = 0.08f + pulse * 0.08f),
                        radius = radius * (0.58f + pulse * 0.16f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.4.dp.toPx()),
                    )
                    drawArc(
                        color = ink.copy(alpha = 0.72f),
                        startAngle = 18f,
                        sweepAngle = 105f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                        ),
                    )
                    drawArc(
                        color = ink.copy(alpha = 0.26f),
                        startAngle = 205f,
                        sweepAngle = 78f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        ),
                    )
                }
                StatusDot(active = true)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "AMADUSE",
                    color = ink,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = "正在载入画布",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ChatScreen(
    live2dWebView: WebView,
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
    onStop: () -> Unit,
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
                        if (horizontalDrag > 92f) {
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
            live2dWebView = live2dWebView,
            settings = settings,
            configuredModels = configuredModels,
            mode = selectedMode,
            sending = sending,
            onSettingsClick = onHistoryOpen,
            onNewChat = onNewChat,
            onModeChange = onModeChange,
            onModelSelect = onModelSelect,
            onModelConfigure = onModelConfigure,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            MainConversationArea(
                live2dWebView = live2dWebView,
                modifier = Modifier.matchParentSize(),
                messages = messages,
                persona = selectedPersona,
                characterExpanded = characterExpanded,
                recording = recording,
                bottomOverlayPadding = 78.dp,
                onCharacterToggle = onCharacterToggle,
                onToggleThinking = onToggleThinking,
            )
            ChatComposer(
                modifier = Modifier.align(Alignment.BottomCenter),
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
                onStop = onStop,
                onSend = onSend,
            )
        }
    }
}

@Composable
private fun ChatHistoryScreen(
    records: List<ChatRecord>,
    selectedRecord: ChatRecord,
    onAvatarClick: () -> Unit,
    onNewChat: () -> Unit,
    onRecordClick: (ChatRecord) -> Unit,
    onDeleteRecord: (ChatRecord) -> Unit,
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
                        if (horizontalDrag < -92f) {
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
                    onExport = { /* 暂不实现 */ },
                    onDelete = { onDeleteRecord(record) },
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
            onClick = {
                android.util.Log.d("Amaduse", "HistoryHeader: Avatar Surface onClick triggered")
                onAvatarClick()
            },
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
    onExport: () -> Unit,
    onDelete: () -> Unit,
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
        Row(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
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
            Row {
                IconButton(
                    onClick = onExport,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = "导出",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "删除",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    persona: PersonaPreset,
    live2dWebView: WebView,
    settings: ModelSettings,
    configuredModels: List<ConfiguredModel>,
    mode: ChatMode,
    sending: Boolean,
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
        EmotionPickerChip(
            personaName = persona.name,
            live2dWebView = live2dWebView,
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
        )
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
private fun EmotionPickerChip(
    personaName: String,
    live2dWebView: WebView,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val dark = isSystemInDarkTheme()
    val panelColor = if (dark) Color(0xEE1C1C1E) else Color(0xEEF7F7F8)
    val itemHover = if (dark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.045f)
    val emotions = listOf(
        "neutral" to "自然",
        "joy" to "开心",
        "smile" to "微笑",
        "shy" to "害羞",
        "surprise" to "惊讶",
        "anger" to "生气",
        "sadness" to "难过",
        "unhappy" to "不满",
    )

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            shape = RoundedCornerShape(999.dp),
            onClick = { expanded = !expanded },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = personaName,
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
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            rotationZ = if (expanded) 180f else 0f
                        },
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = panelColor,
            shape = RoundedCornerShape(AmaduseStyle.PanelRadius),
            shadowElevation = 16.dp,
        ) {
            emotions.forEach { (emotion, label) ->
                EmotionMenuItem(
                    label = label,
                    value = emotion,
                    hoverColor = itemHover,
                    onClick = {
                        live2dWebView.evaluateJavascript("playEmotion('$emotion')", null)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun EmotionMenuItem(
    label: String,
    value: String,
    hoverColor: Color,
    onClick: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val background by animateColorAsState(
        targetValue = if (pressed) hoverColor else Color.Transparent,
        animationSpec = tween(AmaduseMotion.Fast),
        label = "emotion-menu-item-bg",
    )
    Row(
        modifier = Modifier
            .width(148.dp)
            .background(background)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                pressed = true
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusDot(active = false)
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
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
    val dark = isSystemInDarkTheme()
    val availableModels = configuredModels.filter { it.apiKey.isNotBlank() }
    val currentConfigured = configuredModels.firstOrNull { it.id == settings.configuredModelId }
    val modelLabel = currentConfigured?.displayName
        ?: settings.provider.name.takeIf { settings.apiKey.isNotBlank() }
        ?: "未配置"

    Box {
        // Trigger button — glass pill
        Surface(
            modifier = Modifier
                .width(88.dp)
                .height(38.dp)
                .glassLayer(
                    shape = RoundedCornerShape(999.dp),
                    dark = dark,
                ),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(999.dp),
            onClick = { expanded = !expanded },
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
                    modifier = Modifier
                        .size(15.dp)
                        .graphicsLayer {
                            rotationZ = if (expanded) 180f else 0f
                        },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Glass dropdown — focusable=false keeps trigger clickable;
        // transparent fullscreen tap-layer inside Popup handles outside dismiss.
        val borderColor = if (dark) Color.White.copy(alpha = 0.10f)
                          else Color.Black.copy(alpha = 0.06f)
        val highlight = if (dark) Color.White.copy(alpha = 0.08f)
                        else Color.White.copy(alpha = 0.35f)
        val hoverColor = if (dark) Color.White.copy(alpha = 0.06f)
                         else Color.Black.copy(alpha = 0.04f)
        val glassBg = if (dark) Color(0x991C1C1E) else Color(0x99F2F2F7)

        // Popup only exists while open — avoids permanent fullscreen overlay blocking all touches.
        // Entrance animation plays on appear; exit is instant (Popup removed from tree).
        if (expanded) Popup(
            alignment = Alignment.TopStart,
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false),
        ) {
            // Fullscreen transparent tap-layer for outside-click dismiss
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { expanded = false },
            ) {
                Column(
                        modifier = Modifier
                            .padding(top = 46.dp, start = 8.dp)
                            .widthIn(min = 220.dp, max = 280.dp)
                            .heightIn(max = 360.dp)
                            .graphicsLayer {
                                shadowElevation = 20f
                                shape = RoundedCornerShape(AmaduseStyle.PanelRadius)
                                clip = true
                            }
                            .background(glassBg, RoundedCornerShape(AmaduseStyle.PanelRadius))
                            .border(
                                BorderStroke(AmaduseStyle.Hairline, borderColor),
                                RoundedCornerShape(AmaduseStyle.PanelRadius),
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { /* consume tap — don't dismiss when tapping inside dropdown */ },
                    ) {
                        // Top glass highlight band
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AmaduseStyle.Hairline)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, highlight, Color.Transparent),
                                    ),
                                ),
                        )

                        if (availableModels.isEmpty()) {
                            GlassModalItem(
                                title = "暂无可用模型",
                                subtitle = "请先添加 API Key",
                                dark = dark,
                                hoverColor = hoverColor,
                                onClick = {
                                    expanded = false
                                    onModelConfigure()
                                },
                            )
                        }

                        availableModels.forEachIndexed { index, configured ->
                            if (index > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .height(AmaduseStyle.Hairline)
                                        .background(borderColor),
                                )
                            }
                            GlassModalItem(
                                title = configured.displayName,
                                subtitle = configured.model,
                                dark = dark,
                                hoverColor = hoverColor,
                                selected = configured.id == settings.configuredModelId,
                                onClick = {
                                    expanded = false
                                    onModelSelect(configured)
                                },
                            )
                        }

                        if (availableModels.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .height(AmaduseStyle.Hairline)
                                    .background(borderColor),
                            )
                        }

                        GlassModalItem(
                            title = "自定义",
                            subtitle = "跳转到模型配置",
                            dark = dark,
                            hoverColor = hoverColor,
                            trailing = {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
    }
}

@Composable
private fun GlassModalItem(
    title: String,
    subtitle: String,
    dark: Boolean,
    hoverColor: Color,
    selected: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val itemBg by animateColorAsState(
        targetValue = if (pressed) hoverColor else Color.Transparent,
        animationSpec = tween(AmaduseMotion.Fast),
        label = "glass-item-bg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                pressed = true
                onClick()
            }
            .background(itemBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        trailing?.invoke()
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
    live2dWebView: WebView,
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    persona: PersonaPreset,
    characterExpanded: Boolean,
    recording: Boolean,
    bottomOverlayPadding: Dp = 0.dp,
    onCharacterToggle: () -> Unit,
    onToggleThinking: (Int) -> Unit,
) {
    val transcriptExpanded = characterExpanded
    val dark = isSystemInDarkTheme()

    BoxWithConstraints(modifier = modifier) {
        var lockedStageWidth by remember { mutableStateOf(0.dp) }
        var lockedStageHeight by remember { mutableStateOf(0.dp) }
        LaunchedEffect(maxWidth, maxHeight) {
            val widthChanged = lockedStageWidth == 0.dp ||
                maxWidth > lockedStageWidth + 8.dp ||
                maxWidth < lockedStageWidth - 8.dp
            if (widthChanged) {
                lockedStageWidth = maxWidth
                lockedStageHeight = maxHeight
            } else if (maxHeight > lockedStageHeight) {
                lockedStageHeight = maxHeight
            }
        }
        val stageWidth = if (lockedStageWidth > 0.dp) lockedStageWidth else maxWidth
        val stageHeight = if (lockedStageHeight > 0.dp) lockedStageHeight else maxHeight
        val collapsedHeight = maxHeight * 0.34f
        val expandedHeight = maxHeight * 0.84f
        val transcriptHeight by animateDpAsState(
            targetValue = if (transcriptExpanded) expandedHeight else collapsedHeight,
            animationSpec = spring(dampingRatio = 0.86f, stiffness = 240f),
            label = "transcript-height",
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .requiredWidth(stageWidth)
                .requiredHeight(stageHeight)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (dark) {
                            listOf(Color(0xFF09090B), Color(0xFF131517), Color(0xFF080809))
                        } else {
                            listOf(Color(0xFFEFF3F2), Color(0xFFE8ECEF), Color(0xFFF7F7F5))
                        },
                    ),
                ),
        ) {
            Live2dStage(webView = live2dWebView, modifier = Modifier.matchParentSize())
        }

        AnimatedVisibility(
            visible = transcriptExpanded,
            enter = fadeIn(tween(AmaduseMotion.Default)),
            exit = fadeOut(tween(AmaduseMotion.Fast)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (dark) 0.18f else 0.08f)),
            )
        }

        Live2dStatusBadge(
            persona = persona,
            recording = recording,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = AmaduseStyle.ScreenPadding, top = 12.dp),
        )

        FloatingTranscriptPanel(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(transcriptHeight)
                .padding(
                    start = AmaduseStyle.ScreenPadding,
                    end = AmaduseStyle.ScreenPadding,
                    top = if (transcriptExpanded) 10.dp else 6.dp,
                    bottom = bottomOverlayPadding,
                ),
            messages = messages,
            expanded = transcriptExpanded,
            onExpand = { if (!transcriptExpanded) onCharacterToggle() },
            onCollapse = { if (transcriptExpanded) onCharacterToggle() },
            onToggleThinking = onToggleThinking,
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun Live2dStage(webView: WebView, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { webView },
    )
}

@Composable
private fun Live2dStatusBadge(
    persona: PersonaPreset,
    recording: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .glassLayer(
                shape = RoundedCornerShape(999.dp),
                dark = isSystemInDarkTheme(),
            )
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        StatusDot(active = recording)
        Text(
            text = persona.name,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FloatingTranscriptPanel(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onToggleThinking: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(
        topStart = if (expanded) 28.dp else 22.dp,
        topEnd = if (expanded) 28.dp else 22.dp,
        bottomStart = 22.dp,
        bottomEnd = 22.dp,
    )
    val containerModifier = if (expanded) {
        Modifier.glassLayer(shape = shape, dark = isSystemInDarkTheme())
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .then(containerModifier)
            .padding(horizontal = if (expanded) 10.dp else 0.dp, vertical = if (expanded) 8.dp else 0.dp),
    ) {
        TranscriptGrabber(
            expanded = expanded,
            onExpand = onExpand,
            onCollapse = onCollapse,
            modifier = Modifier.fillMaxWidth(),
        )
        ChatTranscript(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            messages = messages,
            expanded = expanded,
            onToggleThinking = onToggleThinking,
        )
    }
}

@Composable
private fun TranscriptGrabber(
    expanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var verticalDrag by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .height(if (expanded) 26.dp else 22.dp)
            .pointerInput(expanded) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount -> verticalDrag += dragAmount },
                    onDragEnd = {
                        if (verticalDrag < -36f) {
                            onExpand()
                        } else if (verticalDrag > 40f) {
                            onCollapse()
                        }
                        verticalDrag = 0f
                    },
                    onDragCancel = { verticalDrag = 0f },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(if (expanded) 48.dp else 42.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (expanded) 0.28f else 0.38f)),
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
    expanded: Boolean,
    onToggleThinking: (Int) -> Unit,
) {
    val listState: LazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val hasStreamingMessage = messages.any { it.streaming }
    val lastMessage = messages.lastOrNull()
    val isAtBottom by remember {
        derivedStateOf { listState.isAtTranscriptBottom() }
    }
    var userDetachedFromBottom by remember { mutableStateOf(false) }
    var autoScrolling by remember { mutableStateOf(false) }
    var previousMessageCount by remember { mutableStateOf(messages.size) }

    suspend fun scrollToTranscriptBottom(animated: Boolean) {
        autoScrolling = true
        try {
            val bottomIndex = transcriptBottomItemIndex(
                messageCount = messages.size,
                hasStreamingMessage = hasStreamingMessage,
            )
            if (animated) {
                listState.animateScrollToItem(bottomIndex)
            } else {
                listState.scrollToItem(bottomIndex)
            }
        } finally {
            autoScrolling = false
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress to listState.isAtTranscriptBottom() }
            .collect { (isScrolling, atBottom) ->
                if (isScrolling && !autoScrolling) {
                    userDetachedFromBottom = true
                }
                if (atBottom) {
                    userDetachedFromBottom = false
                }
            }
    }

    LaunchedEffect(
        messages.size,
        lastMessage?.text?.length,
        lastMessage?.thinking?.length,
        lastMessage?.streaming,
        lastMessage?.thinkingExpanded,
        hasStreamingMessage,
        expanded,
    ) {
        val hasNewMessage = messages.size > previousMessageCount
        previousMessageCount = messages.size
        if (hasNewMessage) {
            userDetachedFromBottom = false
            scrollToTranscriptBottom(animated = false)
            return@LaunchedEffect
        }

        if (!userDetachedFromBottom || isAtBottom) {
            scrollToTranscriptBottom(animated = false)
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                horizontal = if (expanded) 2.dp else 0.dp,
                vertical = if (expanded) 8.dp else 2.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(if (expanded) 14.dp else 10.dp),
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

        AnimatedVisibility(
            visible = !isAtBottom,
            enter = fadeIn(tween(AmaduseMotion.Fast)) + slideInVertically { it / 2 },
            exit = fadeOut(tween(AmaduseMotion.Fast)) + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .glassLayer(
                        shape = CircleShape,
                        dark = isSystemInDarkTheme(),
                    )
                    .clickable {
                        coroutineScope.launch {
                            userDetachedFromBottom = false
                            scrollToTranscriptBottom(animated = true)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "回到底部",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

private fun transcriptBottomItemIndex(
    messageCount: Int,
    hasStreamingMessage: Boolean,
): Int {
    return 1 + messageCount + if (hasStreamingMessage) 1 else 0
}

private fun LazyListState.isAtTranscriptBottom(): Boolean {
    val visibleItems = layoutInfo.visibleItemsInfo
    if (layoutInfo.totalItemsCount == 0 || visibleItems.isEmpty()) {
        return true
    }

    val lastVisible = visibleItems.last()
    return lastVisible.index >= layoutInfo.totalItemsCount - 1 &&
        lastVisible.offset + lastVisible.size <= layoutInfo.viewportEndOffset + 8
}

private fun Modifier.chatBubbleLayer(
    shape: Shape,
    dark: Boolean,
): Modifier {
    val fill = if (dark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF151516).copy(alpha = 0.46f),
                Color(0xFF151516).copy(alpha = 0.28f),
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.58f),
                Color.White.copy(alpha = 0.34f),
            ),
        )
    }
    val stroke = if (dark) {
        Color.White.copy(alpha = 0.14f)
    } else {
        Color.Black.copy(alpha = 0.06f)
    }

    return this
        .clip(shape)
        .background(fill)
        .border(BorderStroke(AmaduseStyle.Hairline, stroke), shape)
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
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = 6.dp,
        bottomEnd = 18.dp,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(AmaduseMotion.Default)),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 328.dp)
                    .chatBubbleLayer(
                        shape = shape,
                        dark = isSystemInDarkTheme(),
                    )
                    .padding(horizontal = 13.dp, vertical = 10.dp),
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
                message.activeToolName?.let { toolName ->
                    ToolCallLoadingLine(toolName = toolName)
                }
                if (message.text.isNotBlank()) {
                    MarkdownMessageText(
                        text = message.text,
                        color = MaterialTheme.colorScheme.onSurface,
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
            if (message.text.isNotBlank()) {
                MessageActionRow(
                    text = message.text,
                    modifier = Modifier
                        .widthIn(max = 328.dp)
                        .padding(start = 4.dp, top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun MessageActionRow(
    text: String,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MessageCopyButton(text = text)
    }
}

@Composable
private fun MessageCopyButton(
    text: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1_100L)
            copied = false
        }
    }

    IconButton(
        onClick = {
            copyMessageText(context, text)
            copied = true
        },
        enabled = text.isNotBlank(),
        modifier = modifier.size(30.dp),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        ),
    ) {
        Icon(
            imageVector = if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
            contentDescription = if (copied) "已复制" else "复制",
            modifier = Modifier.size(16.dp),
        )
    }
}

private fun copyMessageText(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Amaduse message", text))
}

@Composable
private fun ToolCallLoadingLine(toolName: String) {
    val transition = rememberInfiniteTransition(label = "tool-call-loading")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(900, easing = LinearEasing)),
        label = "tool-call-spinner",
    )
    val textAlpha by transition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(820, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tool-call-text-alpha",
    )
    val spinnerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.48f)

    Row(
        modifier = Modifier.padding(top = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Canvas(
            modifier = Modifier
                .size(15.dp)
                .rotate(rotation),
        ) {
            drawArc(
                color = spinnerColor,
                startAngle = 25f,
                sweepAngle = 285f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                ),
            )
        }
        Text(
            text = "正在调用 $toolName ...",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
        )
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
    val dark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(
        topStart = AmaduseStyle.BubbleRadius,
        topEnd = AmaduseStyle.BubbleRadius,
        bottomStart = AmaduseStyle.BubbleRadius,
        bottomEnd = 6.dp,
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 308.dp),
            color = if (dark) Color.White.copy(alpha = 0.09f) else Color.White.copy(alpha = 0.54f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = shape,
            border = BorderStroke(
                AmaduseStyle.Hairline,
                if (dark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.05f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MarkdownMessageText(
                    text = message.text,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (message.attachments.isNotEmpty()) {
                    AttachmentRow(attachments = message.attachments)
                }
            }
        }
        Row(
            modifier = Modifier
                .widthIn(max = 308.dp)
                .padding(end = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MessageActionRow(
                text = message.text,
                horizontalArrangement = Arrangement.End,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = message.time,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
        }
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
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f))
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
    modifier: Modifier = Modifier,
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
    onStop: () -> Unit,
    onSend: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val canSend = !sending && (draft.isNotBlank() || attachments.isNotEmpty())

    Column(
        modifier = modifier
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
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ComposerIconButton(
                icon = if (toolsVisible) Icons.Rounded.Close else Icons.Rounded.Add,
                contentDescription = if (toolsVisible) "关闭工具" else "打开工具",
                active = toolsVisible,
                onClick = onToolsToggle,
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .glassLayer(
                        shape = RoundedCornerShape(AmaduseStyle.ControlRadius),
                        dark = isSystemInDarkTheme(),
                    )
                    .animateContentSize(animationSpec = tween(AmaduseMotion.Default))
                    .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp, max = 136.dp)
                        .padding(vertical = 5.dp),
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
                            onClick = {
                                onStop()
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "中断输出",
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
    voiceSettings: VoiceSettings,
    configuredModels: List<ConfiguredModel>,
    sheetMode: SettingsSheetMode,
    mode: ChatMode,
    onApply: (ModelSettings, VoiceSettings, ChatMode, List<ConfiguredModel>) -> Unit,
    onDismiss: () -> Unit,
) {
    android.util.Log.d("Amaduse", "SettingsSheet composable: visible=$visible, sheetMode=$sheetMode")
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
        var voiceApiKey by remember(visible, voiceSettings) { mutableStateOf(voiceSettings.siliconFlowApiKey) }
        var voiceReferenceSource by remember(visible, voiceSettings) {
            mutableStateOf(voiceSettings.referenceAudioSource.ifBlank { DEFAULT_VOICE_REFERENCE_SOURCE })
        }
        var voiceUri by remember(visible, voiceSettings) { mutableStateOf(voiceSettings.clonedVoiceUri) }
        var voiceAutoPlay by remember(visible, voiceSettings) { mutableStateOf(voiceSettings.autoPlay) }
        var voiceSyncOutput by remember(visible, voiceSettings) { mutableStateOf(voiceSettings.syncTextOutput) }
        var voiceStatus by remember(visible) { mutableStateOf("") }
        var voiceCloning by remember(visible) { mutableStateOf(false) }
        var selectedMode by remember(visible, mode) { mutableStateOf(mode) }
        val sheetCoroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        val voiceFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
                voiceReferenceSource = it.toString()
                voiceStatus = "已选择本地参考音频。克隆前请确认参考文本与音频内容一致。"
            }
        }
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
                                icon = Icons.Rounded.Computer,
                                title = "连接电脑",
                                value = "未连接",
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

                    SettingsSection(title = "硅基流动语音") {
                        Text(
                            text = "默认使用 assets 下的 Kurisu 参考音频，也可以选择本地音频文件。克隆成功后保存返回的 speech: voice uri；之后播报只调用 TTS。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                        )
                        SettingsTextInput(
                            label = "SiliconFlow API Key",
                            value = voiceApiKey,
                            onValueChange = { voiceApiKey = it },
                            placeholder = "sk-...",
                            secret = true,
                        )
                        SettingsTextInput(
                            label = "参考音频地址",
                            value = voiceReferenceSource,
                            onValueChange = { voiceReferenceSource = it },
                            placeholder = DEFAULT_VOICE_REFERENCE_SOURCE,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionPill(
                                text = "选择本地音频",
                                selected = false,
                                onClick = {
                                    voiceFilePicker.launch(arrayOf("audio/*", "application/octet-stream"))
                                },
                                modifier = Modifier.weight(1f),
                                icon = Icons.Rounded.AttachFile,
                            )
                            ActionPill(
                                text = "恢复默认",
                                selected = false,
                                onClick = {
                                    voiceReferenceSource = DEFAULT_VOICE_REFERENCE_SOURCE
                                    voiceStatus = "已恢复为 assets 下的 Kurisu 默认参考音频。"
                                },
                                modifier = Modifier.weight(1f),
                                icon = Icons.Rounded.Check,
                            )
                        }
                        SettingsTextInput(
                            label = "Voice URI",
                            value = voiceUri,
                            onValueChange = { voiceUri = it },
                            placeholder = "speech:...",
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionPill(
                                text = if (voiceCloning) "克隆中" else "克隆参考音频",
                                selected = true,
                                onClick = {
                                    if (voiceCloning) {
                                        return@ActionPill
                                    }
                                    if (voiceApiKey.isBlank()) {
                                        voiceStatus = "请先填写 SiliconFlow API Key。"
                                        return@ActionPill
                                    }
                                    voiceCloning = true
                                    voiceStatus = "正在上传参考音频并克隆..."
                                    sheetCoroutineScope.launch {
                                        val result = runCatching {
                                            withContext(Dispatchers.IO) {
                                                SiliconFlowVoiceService.cloneVoiceFromSource(
                                                    context = context,
                                                    apiKey = voiceApiKey.trim(),
                                                    audioSource = voiceReferenceSource.trim()
                                                        .ifBlank { DEFAULT_VOICE_REFERENCE_SOURCE },
                                                    referenceText = KURISU_REFERENCE_TEXT,
                                                    customName = "amaduse_kurisu_${System.currentTimeMillis()}",
                                                )
                                            }
                                        }
                                        result
                                            .onSuccess {
                                                voiceUri = it
                                                voiceStatus = "克隆完成，已写入 Voice URI。保存设置后生效。"
                                            }
                                            .onFailure {
                                                voiceStatus = "克隆失败：${it.message ?: it.javaClass.simpleName}"
                                            }
                                        voiceCloning = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                icon = Icons.Rounded.Mic,
                            )
                            ActionPill(
                                text = "测试播放",
                                selected = false,
                                onClick = {
                                    val voice = voiceUri.ifBlank {
                                        personaPresets.firstOrNull { it.ttsVoiceId != null }?.ttsVoiceId.orEmpty()
                                    }
                                    if (voiceApiKey.isBlank() || voice.isBlank()) {
                                        voiceStatus = "请先填写 API Key 和 Voice URI。"
                                        return@ActionPill
                                    }
                                    voiceStatus = "正在生成测试语音..."
                                    sheetCoroutineScope.launch {
                                        val result = runCatching {
                                            withContext(Dispatchers.IO) {
                                                SiliconFlowVoiceService.synthesizeSpeechToCacheFile(
                                                    context = context,
                                                    apiKey = voiceApiKey.trim(),
                                                    input = "助手？别用那种奇怪的称呼。有什么问题就快点说。",
                                                    voice = voice,
                                                )
                                            }
                                        }
                                        result
                                            .onSuccess {
                                                SiliconFlowVoiceService.playAudioFile(it)
                                                voiceStatus = "测试语音已生成并播放。"
                                            }
                                            .onFailure {
                                                voiceStatus = "测试失败：${it.message ?: it.javaClass.simpleName}"
                                            }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                icon = Icons.Rounded.Bolt,
                            )
                        }
                        ActionPill(
                            text = if (voiceAutoPlay) "自动播报：开" else "自动播报：关",
                            selected = voiceAutoPlay,
                            onClick = { voiceAutoPlay = !voiceAutoPlay },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Check,
                        )
                        ActionPill(
                            text = if (voiceSyncOutput) "语音文本同步：开" else "语音文本同步：关",
                            selected = voiceSyncOutput,
                            onClick = { voiceSyncOutput = !voiceSyncOutput },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Bolt,
                        )
                        Text(
                            text = "同步开启时，会按约 50 个非符号字符分段翻译并生成语音，文本随对应语音流式显示；后续分段会提前合成并按顺序播放。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                        )
                        if (voiceStatus.isNotBlank()) {
                            Text(
                                text = voiceStatus,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp,
                            )
                        }
                    }

                    if (sheetMode == SettingsSheetMode.Full) {
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
                                VoiceSettings(
                                    siliconFlowApiKey = voiceApiKey.trim(),
                                    referenceAudioSource = voiceReferenceSource.trim()
                                        .ifBlank { DEFAULT_VOICE_REFERENCE_SOURCE },
                                    clonedVoiceUri = voiceUri.trim(),
                                    autoPlay = voiceAutoPlay,
                                    syncTextOutput = voiceSyncOutput,
                                ),
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
private fun NewChatConfirmSheet(
    visible: Boolean,
    onConfirm: () -> Unit,
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
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .glassLayer(
                        shape = RoundedCornerShape(28.dp),
                        dark = isSystemInDarkTheme(),
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {},
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "新建对话",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "当前对话将被保存，是否创建新对话？",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ActionPill(
                        text = "取消",
                        selected = false,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    ActionPill(
                        text = "确认",
                        selected = true,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmSheet(
    visible: Boolean,
    recordTitle: String,
    onConfirm: () -> Unit,
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
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .glassLayer(
                        shape = RoundedCornerShape(28.dp),
                        dark = isSystemInDarkTheme(),
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {},
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "删除对话",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "确定要删除「${recordTitle}」吗？此操作不可撤销。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ActionPill(
                        text = "取消",
                        selected = false,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    ActionPill(
                        text = "删除",
                        selected = true,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AmaduseAppPreview() {
    AmaduseTheme {
        AmaduseApp()
    }
}
