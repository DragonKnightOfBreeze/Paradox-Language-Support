package icu.windea.pls.ai.prompts

/**
 * 指令的注册表。
 *
 * @see PromptTemplateDirective
 */
object PromptTemplateDirectiveRegistry {
    val directives: MutableSet<PromptTemplateDirective> = mutableSetOf()

    init {
        registerDefaults()
    }

    private fun registerDefaults() {
        directives += IncludePromptTemplateDirective
        directives += IfPromptTemplateDirective
        directives += ElseIfPromptTemplateDirective
        directives += ElsePromptTemplateDirective
        directives += EndIfPromptTemplateDirective
    }

    /**
     * 根据注释文本（已 trim）匹配已注册的指令，如果无法匹配则返回null。
     */
    fun match(trimmedComment: String): PromptTemplateDirective? {
        return directives.firstOrNull { it.matches(trimmedComment) }
    }
}
