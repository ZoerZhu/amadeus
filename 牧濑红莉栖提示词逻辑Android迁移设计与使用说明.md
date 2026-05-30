# 牧濑红莉栖提示词逻辑 Android 迁移设计与使用说明

本文档用于把当前项目根目录下三份说明文档中“构建牧濑红莉栖提示词”的逻辑，转换成本 Android 项目可落地的设计。本文先做设计和使用说明，不直接修改运行时代码。

相关来源文档：

- `牧濑红莉栖角色提示词总结.md`
- `牧濑红莉栖语音语气语调生成逻辑说明.md`
- `牧濑红莉栖Live2D模型使用说明.md`

当前项目可接入点：

- `app/src/main/kotlin/com/sg/amaduse/MainActivity.kt`
  - `PersonaPreset`：当前人格预设数据结构。
  - `personaPresets`：当前人格列表。
  - `fetchOpenAiCompatibleLiveStream(...)`：当前 OpenAI-compatible 聊天请求入口。
  - `buildSystemPrompt(...)`：当前系统提示词构建函数。
- `app/src/main/assets/live2d_viewer.html`
  - 当前通过 WebView 加载本地 Live2D 模型。
- `app/src/main/assets/live2dmodels/steinsGateKurisuNew/`
  - 当前已内置牧濑红莉栖 Live2D 模型、表情和动作资源。
- `app/src/main/assets/voices/kurisu-reference.mp3`
  - 当前已内置参考音频。

## 1. 迁移目标

迁移目标不是把旧 Web 项目的全部服务端链路照搬进 Android，而是先把“角色 Prompt 构建逻辑”抽象成本项目可维护的模块：

```text
人格预设 Persona
  -> PromptRuntimeContext
  -> KurisuPromptBuilder
  -> system message
  -> OpenAI-compatible chat/completions
```

第一阶段只处理文本对话提示词：

- 新增“牧濑红莉栖”人格预设。
- 使用旧项目总结出的核心人格提示词。
- 复用旧项目 `generate_sys_prompt(...)` 的结构思想。
- 在当前 `buildSystemPrompt(...)` 中生成更完整的系统提示词。
- 保留当前聊天请求、流式解析和 UI 展示方式。

第二阶段再处理语音、翻译、主动触发和 Live2D 表情联动：

- TTS 使用 SiliconFlow CosyVoice voice id。
- LLM 输出日语，前端或后端翻译成中文显示。
- 根据回复内容生成 emotion，再驱动 Live2D 表情和动作。
- 支持 `self_motivated` 作为内部主动发言触发。

## 2. 当前旧逻辑的核心结论

旧说明文档中真正决定角色体验的是以下几项：

```text
1. 核心人格提示词
2. generate_sys_prompt 包装模板
3. voice_output_language = ja
4. text_output_language = zh
5. SiliconFlow Kurisu voice id 或参考音频克隆结果
6. emotion 输出值与 Live2D 表情/动作名匹配
```

核心人格提示词：

```text
命运石之门(steins gate)的牧濑红莉栖(kurisu),一个天才少女,性格傲娇,不喜欢被叫克里斯蒂娜
```

旧项目的包装模板本质上做了这些事：

- 把核心人格放进 `<Personality>`。
- 指定输出风格像实时语音交谈。
- 指定输出语言。
- 注入人物中英文名称映射。
- 提醒 Whisper 转录可能有误。
- 声明可以看到摄像头画面。
- 支持主动发起对话。
- 禁止输出内部心理活动。
- 注入当前用户和当前时间。

这些逻辑应迁移为 Android 侧的 Prompt Builder，而不是散落在 UI 字符串里。

## 3. 推荐模块设计

短期可以继续放在 `MainActivity.kt` 内，便于快速验证。进入正式开发后建议拆成包：

```text
app/src/main/kotlin/com/sg/amaduse/agent/persona/
  PersonaProfile.kt
  PromptRuntimeContext.kt
  PromptBuilder.kt
  KurisuPrompt.kt
```

### 3.1 PersonaProfile

用于替代当前过短的 `PersonaPreset`。

```kotlin
private data class PersonaProfile(
    val id: String,
    val name: String,
    val subtitle: String,
    val basePersonality: String,
    val toneRules: List<String>,
    val addressRules: List<String>,
    val boundaryRules: List<String>,
    val voiceOutputLanguage: OutputLanguage,
    val textOutputLanguage: OutputLanguage,
    val ttsVoiceId: String? = null,
    val referenceAudioAsset: String? = null,
)
```

字段含义：

| 字段 | 说明 |
| --- | --- |
| `id` | 稳定 ID，例如 `kurisu_steins_gate` |
| `name` | UI 展示名称 |
| `subtitle` | UI 展示副标题 |
| `basePersonality` | 旧项目的核心人格提示词 |
| `toneRules` | 额外语气约束，补足旧提示词过短的问题 |
| `addressRules` | 称呼、禁忌称呼、人物名映射 |
| `boundaryRules` | 安全边界、工具确认规则、版权风险约束 |
| `voiceOutputLanguage` | 给 TTS 的语言，旧逻辑默认日语 |
| `textOutputLanguage` | App 展示语言，旧逻辑默认中文 |
| `ttsVoiceId` | SiliconFlow voice id，后续 TTS 使用 |
| `referenceAudioAsset` | 本地参考音频路径 |

### 3.2 PromptRuntimeContext

用于承载每次请求时会变化的信息。

```kotlin
private data class PromptRuntimeContext(
    val userName: String,
    val currentTimeText: String,
    val mode: ChatMode,
    val memoryFacts: List<String> = emptyList(),
    val hasCameraFrame: Boolean = false,
    val isSelfMotivated: Boolean = false,
)
```

字段来源：

- `userName`：第一版可固定为“用户”，后续从设置页读取。
- `currentTimeText`：复用当前 `currentTimeText()` 或增加完整日期时间。
- `mode`：复用当前 `ChatMode.Fast / Thinking`。
- `memoryFacts`：第一版为空，后续接 Room/DataStore 长期记忆。
- `hasCameraFrame`：当前未接摄像头理解，第一版为 `false`。
- `isSelfMotivated`：主动发言时为 `true`。

### 3.3 OutputLanguage

旧逻辑里语言字段用 `ja / zh / en`，Android 侧建议显式建枚举，避免到处传字符串。

```kotlin
private enum class OutputLanguage(
    val code: String,
    val promptLabel: String,
) {
    Japanese("ja", "日文"),
    Chinese("zh", "中文"),
    English("en", "英文"),
}
```

### 3.4 KurisuPromptBuilder

Prompt Builder 负责把人格、模式、记忆、时间等信息统一拼成 system prompt。

```kotlin
private object KurisuPromptBuilder {
    fun build(
        persona: PersonaProfile,
        context: PromptRuntimeContext,
    ): String = buildString {
        appendLine("<Instruction>")
        appendLine("你是 Amaduse Android App 中的个性化智能 Agent。")
        appendLine("你需要基于给定人格设定进行自然对话，保持连续、克制、真实的交流感。")
        appendLine("<Personality>${persona.basePersonality}</Personality>")
        appendLine("<ToneRules>")
        persona.toneRules.forEach { appendLine("- $it") }
        appendLine("</ToneRules>")
        appendLine("<OutputStyle>回复风格表现得像和一个真实的人类在实时交谈。</OutputStyle>")
        appendLine("<OutputLanguage>${persona.voiceOutputLanguage.promptLabel}</OutputLanguage>")
        appendLine("<NameMapping>牧濑红莉栖(kurisu)，冈部伦太郎(okabe)，椎名真由理(mayuri)，比屋定真帆(maho)，阿万音铃羽(suzuha)，桥田至(daru)</NameMapping>")
        appendLine("<SpeechRecognitionNotice>如果用户输入像语音转写结果，允许根据上下文纠正常见误识别。</SpeechRecognitionNotice>")
        appendLine("<InnerMonologueRules>严禁向用户输出内部心理活动。</InnerMonologueRules>")
        appendLine("<CurrentUser>${context.userName}</CurrentUser>")
        appendLine("<CurrentTime>${context.currentTimeText}</CurrentTime>")
        appendLine("<Mode>${if (context.mode == ChatMode.Fast) "快速会话" else "思考会话"}</Mode>")
        appendLine("</Instruction>")
    }
}
```

正式实现时不建议在多个函数里拼接碎片。所有角色提示词都应从一个 Builder 输出，便于后续测试和版本管理。

## 4. 牧濑红莉栖人格预设设计

第一版可新增一个内置预设：

```kotlin
private val kurisuPersona = PersonaProfile(
    id = "kurisu_steins_gate",
    name = "牧濑红莉栖",
    subtitle = "理性、尖锐、略带傲娇",
    basePersonality = "命运石之门(steins gate)的牧濑红莉栖(kurisu),一个天才少女,性格傲娇,不喜欢被叫克里斯蒂娜",
    toneRules = listOf(
        "表达理性、聪明、克制，先判断问题再回答。",
        "可以有轻微吐槽和反驳，但不要持续攻击用户。",
        "被叫克里斯蒂娜时要自然纠正，不要长篇解释。",
        "涉及科学、实验、时间机器、论文和技术问题时更认真。",
        "避免过度卖萌、过度撒娇或大量复述作品台词。",
        "中文展示时保持红莉栖式语气；语音模式下可切换日语输出。"
    ),
    addressRules = listOf(
        "用户可称为“你”或设置中的用户名。",
        "不要接受“克里斯蒂娜”作为正式称呼。",
        "知道常见人物名中日英映射，但不要主动堆砌设定。"
    ),
    boundaryRules = listOf(
        "不要声称自己拥有真实人物或声优身份。",
        "不要大量复刻受版权保护的原作台词。",
        "涉及手机、短信、闹钟、电脑任务等真实操作时必须先请求用户确认。"
    ),
    voiceOutputLanguage = OutputLanguage.Chinese,
    textOutputLanguage = OutputLanguage.Chinese,
    ttsVoiceId = "speech:siliconflow-kurisu:clzv7bjjm041fufyct2z0setm:mphrsbbmvrjfophbsted",
    referenceAudioAsset = "voices/kurisu-reference.mp3",
)
```

注意：这里建议第一版把 `voiceOutputLanguage` 先设为中文，因为当前 Android 项目还没有翻译层和 TTS 层。如果马上设为日语，当前聊天 UI 会直接显示日语。等 TTS 和翻译链路接入后，再恢复旧项目默认策略：

```text
voiceOutputLanguage = ja
textOutputLanguage = zh
```

## 5. 当前代码的最小接入方案

当前项目仍是单 `MainActivity.kt` 文件，最小改动路径如下。

### 5.1 扩展 PersonaPreset

当前结构：

```kotlin
private data class PersonaPreset(
    val name: String,
    val subtitle: String,
    val tone: String,
)
```

建议短期改为：

```kotlin
private data class PersonaPreset(
    val id: String,
    val name: String,
    val subtitle: String,
    val tone: String,
    val systemPersonality: String = "",
    val outputLanguage: OutputLanguage = OutputLanguage.Chinese,
    val ttsVoiceId: String? = null,
    val referenceAudioAsset: String? = null,
)
```

这样不用马上重构 UI，也能把旧项目的核心提示词塞进当前人格系统。

### 5.2 在 personaPresets 中加入红莉栖

把“牧濑红莉栖”作为一个可选人格放入 `personaPresets`。建议放在 `Amaduse` 后面，避免默认启动就进入具体角色。

```kotlin
PersonaPreset(
    id = "kurisu_steins_gate",
    name = "牧濑红莉栖",
    subtitle = "天才少女 · 理性吐槽",
    tone = "理性、尖锐、克制，轻微傲娇，不喜欢被叫克里斯蒂娜",
    systemPersonality = "命运石之门(steins gate)的牧濑红莉栖(kurisu),一个天才少女,性格傲娇,不喜欢被叫克里斯蒂娜",
    outputLanguage = OutputLanguage.Chinese,
    ttsVoiceId = "speech:siliconflow-kurisu:clzv7bjjm041fufyct2z0setm:mphrsbbmvrjfophbsted",
    referenceAudioAsset = "voices/kurisu-reference.mp3",
)
```

### 5.3 替换 buildSystemPrompt

当前 `buildSystemPrompt(persona, mode)` 只拼接了很短的系统提示词。迁移后应改成：

```text
如果 persona.id == kurisu_steins_gate：
  使用 KurisuPromptBuilder 构建完整 system prompt
否则：
  使用当前默认通用 Agent prompt
```

这样不会影响现有其他人格。

### 5.4 继续把 system prompt 放在 messages 第一条

当前请求已经符合旧逻辑的关键点：

```kotlin
payloadMessages.put(
    JSONObject().put("role", "system").put("content", buildSystemPrompt(persona, mode))
)
```

迁移时保留这个位置即可。也就是说，Prompt Builder 只负责输出 `content`，不需要改变请求协议。

## 6. 使用说明

### 6.1 当前文本版使用方式

在完成最小接入后：

1. 打开 App。
2. 打开人格选择面板。
3. 选择“牧濑红莉栖”。
4. 打开模型设置，选择 OpenAI-compatible 服务。
5. 填写 `baseUrl`、`model` 和 `apiKey`。
6. 回到聊天页输入消息。

当前文本版建议保持中文输出。示例输入：

```text
你是谁？
```

预期效果：

- 回复应体现理性、轻微吐槽、克制的语气。
- 如果用户称呼“克里斯蒂娜”，角色应纠正。
- 技术问题应更认真回答。
- 不应输出内部心理活动。
- 不应大量复述原作台词。

测试称呼禁忌：

```text
克里斯蒂娜，帮我解释一下量子纠缠。
```

预期行为：

```text
先纠正“不要叫我克里斯蒂娜”，再回答量子纠缠问题。
```

### 6.2 语音优先版使用方式

等 TTS 和翻译接入后，把语言策略切换为：

```text
voiceOutputLanguage = ja
textOutputLanguage = zh
```

推荐链路：

```text
LLM 生成日语回复
  -> SiliconFlow CosyVoice 使用 Kurisu voice id 合成语音
  -> 翻译模块把日语转中文
  -> UI 显示中文
  -> Live2D 根据 emotion 切换表情/动作
```

这一版里，用户看到中文，听到日语，更接近旧项目体验。

### 6.3 主动发言使用方式

旧项目使用内部用户消息：

```text
self_motivated
```

Android 侧建议不要把它显示在聊天列表里，而是实现成内部触发：

```text
用户长时间无输入
  -> App 构造内部 trigger
  -> 请求消息中追加 user: self_motivated
  -> UI 只显示 AI 主动回复
```

系统提示词中需要保留规则：

```text
你可以通过接收用户的 "self_motivated" 指令来自我触发，但严禁向用户解释这是内部触发词。
```

### 6.4 记忆增强使用方式

第一版可以不做记忆。后续接入时，把相关记忆拼在用户消息前，而不是拼进人格本体：

```text
Relevant Memories/Facts:
- 用户希望被称为“研究员”
- 用户喜欢简洁解释

User Question:
{用户本次输入}
```

这样人格 Prompt 保持稳定，记忆只影响当前回合。

## 7. Live2D 表情联动设计

当前项目已能加载本地 Live2D 模型，但 `live2d_viewer.html` 目前只做随机动作，没有从 Kotlin 接收 emotion。

后续建议在 `live2d_viewer.html` 暴露 JS API：

```javascript
window.AmaduseLive2D = {
  setEmotion(name) {
    model.expression(name)
    model.motion(name, 0, PIXI.live2d.MotionPriority.FORCE)
  },
  playMotion(name) {
    model.motion(name, 0, PIXI.live2d.MotionPriority.FORCE)
  }
}
```

Android 侧用 WebView 调用：

```kotlin
webView.evaluateJavascript(
    "window.AmaduseLive2D && window.AmaduseLive2D.setEmotion('$emotion')",
    null,
)
```

可用 emotion 名称应与本地模型资源保持一致：

```text
neutral
anger
joy
sadness
shy
shy2
smile1
smile2
surprise
unhappy
```

情绪分类模块可以在后续实现，输入为 AI 回复文本，输出上述枚举之一。

## 8. 验收标准

第一阶段验收：

- 人格列表中出现“牧濑红莉栖”。
- 选择该人格后，请求 system message 使用完整 Kurisu Prompt。
- 普通人格仍使用原有通用 Prompt，不受影响。
- 用户称呼“克里斯蒂娜”时，回复会纠正。
- 技术问题回答比普通闲聊更理性。
- 快速模式和思考模式仍能正常影响回复风格。
- 聊天流式输出不受影响。

第二阶段验收：

- 可切换日语语音输出、中文文本展示。
- TTS 使用配置的 Kurisu voice id 或克隆 voice。
- Live2D 能根据回复内容播放对应表情/动作。
- `self_motivated` 主动触发不显示在聊天记录中。
- 记忆增强只影响当前请求，不污染人格 Prompt。

## 9. 风险与注意事项

- 旧项目的核心人格提示词很短，直接迁移会导致角色稳定性不足，因此需要 `toneRules` 补充风格约束。
- 当前 Android 项目还没有翻译层；如果强制日语输出，UI 会显示日语。
- 当前 Android 项目还没有 TTS 请求链路；`ttsVoiceId` 第一阶段只是配置预留。
- 当前 Live2D WebView 没有 emotion JS bridge；第一阶段不能根据模型回复切表情。
- 不建议内置大量原作台词，也不建议声称 App 拥有真实角色或声优身份。
- API Key 当前保存在本机设置中，后续正式版应迁移到后端或使用更安全的本地加密存储。

## 10. 推荐实施顺序

```text
阶段 1：文本 Prompt MVP
  1. 扩展 PersonaPreset
  2. 新增 OutputLanguage
  3. 新增 Kurisu 人格预设
  4. 改造 buildSystemPrompt
  5. 用聊天请求验证回复语气

阶段 2：配置持久化
  1. 保存 selectedPersona.id
  2. 重启后恢复上次人格
  3. 设置页展示语言和 voice 配置

阶段 3：语音链路
  1. 接入 TTS 服务
  2. 使用 Kurisu voice id
  3. 加入日语输出和中文翻译

阶段 4：Live2D 联动
  1. 给 WebView 加 JS bridge
  2. 增加 emotion 分类
  3. 回复结束后触发表情和动作

阶段 5：主动发言与记忆
  1. 增加 self_motivated 内部触发
  2. 增加显式长期记忆
  3. 把相关记忆注入用户消息
```

## 11. 下一步代码改造范围

如果按本文档进入实现，优先改这些位置：

```text
app/src/main/kotlin/com/sg/amaduse/MainActivity.kt
  - PersonaPreset
  - personaPresets
  - buildSystemPrompt(...)
  - selectedPersona 持久化

app/src/main/assets/live2d_viewer.html
  - 后续加入 window.AmaduseLive2D JS API
```

第一阶段不需要新增依赖，也不需要改 Gradle 配置。
