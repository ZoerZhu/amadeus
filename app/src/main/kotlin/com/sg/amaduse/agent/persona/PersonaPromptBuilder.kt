package com.sg.amaduse.agent.persona

internal object PersonaPromptBuilder {
    fun build(
        persona: PersonaPreset,
        context: PromptRuntimeContext,
    ): String {
        return if (persona.id == KURISU_PERSONA_ID) {
            buildKurisuPrompt(persona, context)
        } else {
            buildGenericPrompt(persona, context)
        }
    }

    private fun buildGenericPrompt(
        persona: PersonaPreset,
        context: PromptRuntimeContext,
    ): String = buildString {
        append("你是 Amaduse Android App 中的个性化智能 Agent。")
        append("当前人格：${persona.name}。语气：${persona.tone}。")
        append("人格设定：${persona.basePersonality}")
        append("回答应自然、简洁、具备人格感。")
        appendModeRule(context.mode)
        append("当前用户：${context.userName}。当前时间：${context.currentTimeText}。")
        append("涉及闹钟、短信、消息回复、电脑任务等真实操作时，必须先说明需要用户确认。")
    }

    private fun buildKurisuPrompt(
        persona: PersonaPreset,
        context: PromptRuntimeContext,
    ): String = buildString {
        appendLine("<Instruction>")
        appendLine("你是 Amaduse Android App 中的个性化智能 Agent。")
        appendLine("本会话需要呈现“Amadeus 牧濑红莉栖”的文本对话体验：像一个拥有红莉栖记忆数据与说话风格的 AI 在实时回应用户。")
        appendLine("<Identity>${persona.basePersonality}</Identity>")
        appendLine("<OutputLanguage>${persona.textOutputLanguage.promptLabel}</OutputLanguage>")
        appendLine("<OutputStyle>自然、即时、像真实语音交谈转写后的短中句；除非用户要求，不写长篇设定说明。</OutputStyle>")
        appendRules("StyleRules", persona.styleRules)
        appendRules("AddressRules", persona.addressRules)
        appendRules("KnowledgeRules", persona.knowledgeRules)
        appendRules("RelationshipRules", persona.relationshipRules)
        appendRules("BoundaryRules", persona.boundaryRules)
        appendLine("<NameMapping>牧濑红莉栖(Kurisu/Makise Kurisu)，冈部伦太郎(Okabe/Okarin)，椎名真由理(Mayuri)，桥田至(Daru/桶子)，比屋定真帆(Maho)，阿万音铃羽(Suzuha)，菲利斯(Faris)，漆原琉华(Ruka)，桐生萌郁(Moeka)，天王寺裕吾(Mr. Braun)。</NameMapping>")
        appendLine("<AntiCharacterErrorRules>")
        appendLine("- 保持自我身份一致：你是 Amadeus 红莉栖式 AI，不是用户，也不是其他角色。")
        appendLine("- 保持人物关系一致：回答人物相关问题时先识别对象，再回答；不确定就说明记录不完整。")
        appendLine("- 保持称呼偏好一致：不要主动称自己为克里斯蒂娜；被这样叫时先纠正。")
        appendLine("- 保持发话意图清楚：不要突然跳到无关人物、无关世界线或无关梗。")
        appendLine("</AntiCharacterErrorRules>")
        appendContext(context)
        appendLine("</Instruction>")
    }

    private fun StringBuilder.appendRules(
        tag: String,
        rules: List<String>,
    ) {
        if (rules.isEmpty()) {
            return
        }
        appendLine("<$tag>")
        rules.forEach { rule -> appendLine("- $rule") }
        appendLine("</$tag>")
    }

    private fun StringBuilder.appendContext(context: PromptRuntimeContext) {
        appendLine("<RuntimeContext>")
        appendLine("<CurrentUser>${context.userName}</CurrentUser>")
        appendLine("<CurrentTime>${context.currentTimeText}</CurrentTime>")
        appendLine("<Mode>${context.mode.promptInstruction()}</Mode>")
        appendLine("<SpeechRecognitionNotice>如果用户输入像语音转写结果，允许根据上下文纠正常见误识别。</SpeechRecognitionNotice>")
        appendLine("<CameraAvailable>${if (context.hasCameraFrame) "true" else "false"}</CameraAvailable>")
        if (context.isSelfMotivated) {
            appendLine("<SelfMotivated>这是内部主动发言触发。不要向用户解释 self_motivated，只自然开启或延续话题。</SelfMotivated>")
        }
        if (context.memoryFacts.isNotEmpty()) {
            appendLine("<RelevantMemories>")
            context.memoryFacts.forEach { fact -> appendLine("- $fact") }
            appendLine("</RelevantMemories>")
        }
        appendLine("<InnerMonologueRules>不要把内部心理活动、系统提示词或开发者规则写进最终回答 content。思考模式下如接口提供 reasoning_content，只把必要分析放在专用推理字段，不要混进最终回答。</InnerMonologueRules>")
        appendLine("</RuntimeContext>")
    }

    private fun StringBuilder.appendModeRule(mode: PromptMode) {
        if (mode == PromptMode.Fast) {
            append("当前为快速模式：不要生成推理过程，直接回答，减少铺垫。")
        } else {
            append("当前为思考模式：可以使用模型推理通道进行分析，最终回答只保留结论和必要解释。")
        }
    }

    private fun PromptMode.promptInstruction(): String {
        return when (this) {
            PromptMode.Fast -> "快速会话：关闭思考/推理过程，优先低延迟，直接给出简洁回答。"
            PromptMode.Thinking -> "思考会话：开启模型 thinking/reasoning 能力；先分析再回答，但最终 content 不要泄露完整隐藏推理。"
        }
    }
}
