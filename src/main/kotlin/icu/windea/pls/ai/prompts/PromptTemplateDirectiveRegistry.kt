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
}
