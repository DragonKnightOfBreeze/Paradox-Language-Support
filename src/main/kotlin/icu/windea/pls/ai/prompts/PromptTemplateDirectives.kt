package icu.windea.pls.ai.prompts

/**
 * include 指令。用于导入其他模版。
 *
 * 参数：
 * - `path` - 相对于当前模版文件的资源路径（如 `prompts/includes/header.md`）
 */
object IncludePromptTemplateDirective : PromptTemplateDirective {
    override val name = "include"
    override val type = PromptTemplateDirective.Type.Inline
}

/**
 * if 指令。用于条件判断，与 `elseif` `else` `endif` 指令组合使用。
 *
 * 参数：
 * - `condition` - 条件表达式。目前仅支持两种格式，`var` 与 `!var`，其中 `var` 是模版变量名。
 */
object IfPromptTemplateDirective : PromptTemplateDirective {
    override val name = "if"
    override val type = PromptTemplateDirective.Type.Block
}

/**
 * elseif 指令。用于条件判断，与 `if` `else` `endif` 指令组合使用。
 *
 * 参数：
 * - `condition` - 条件表达式。目前仅支持两种格式，`var` 与 `!var`，其中 `var` 是模版变量名。
 */
object ElseIfPromptTemplateDirective : PromptTemplateDirective {
    override val name = "elseif"
    override val type = PromptTemplateDirective.Type.Block
}

/**
 * else 指令。用于条件判断，与 `if` `elseif` `endif` 指令组合使用。
 */
object ElsePromptTemplateDirective : PromptTemplateDirective {
    override val name = "else"
    override val type = PromptTemplateDirective.Type.Block
}

/**
 * endif 指令。用于条件判断，与 `if` `elseif` `else` 指令组合使用。
 */
object EndIfPromptTemplateDirective : PromptTemplateDirective {
    override val name = "endif"
    override val type = PromptTemplateDirective.Type.Block
}
