package com.sg.amaduse

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image

internal val modelProviderPresets = listOf(
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
        name = "小米 MiMo",
        baseUrl = "https://api.xiaomimimo.com/v1",
        defaultModel = "mimo-v2.5-pro",
        compatible = true,
        note = "MiMo OpenAI-compatible 接口，快速模式关闭 thinking，思考模式开启 thinking",
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

internal val toolShortcuts = listOf(
    ToolShortcut("相机", "拍照提问", Icons.Rounded.CameraAlt),
    ToolShortcut("图片", "上传视觉上下文", Icons.Rounded.Image),
    ToolShortcut("文件", "附加资料", Icons.Rounded.AttachFile),
    ToolShortcut("闹钟", "创建提醒", Icons.Rounded.Alarm),
    ToolShortcut("备忘", "创建日历任务", Icons.Rounded.Description),
    ToolShortcut("电脑", "发送 PC 任务", Icons.Rounded.Computer),
)

internal val chatRecords = listOf(
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
