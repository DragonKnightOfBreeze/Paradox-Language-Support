package icu.windea.pls.ai.prompts

/**
 * 提示模版的指令。
 *
 * 指令是一种特殊的 Markdown/HTML 单行注释，
 * 格式为 `<!-- @{directiveName} {params...} -->`。
 *
 * 以 `@` 前缀开始，之后是指令名称。
 * 在空白之后是一组指令参数，用空白分隔。
 * 注释的开始标记之后与结束标记之前的空白会被忽略。
 *
 * @property name 指令名称。由小写字母与连字符组成，不含 `@` 前缀。
 * @property type 指令类型。
 */
interface PromptTemplateDirective {
    val name: String
    val type: Type

    /**
     * 指令的类型。
     */
    enum class Type {
        /** 内联指令。内联指令不需要与其他指令结合使用。 */
        Inline,
        /** 块指令。块指令需要与其他指令结合使用。 */
        Block,
        ;
    }
}
