package icu.windea.pls.ai.prompts

/**
 * 提示模板的指令。
 *
 * 指令必须放在 Markdown/HTML 注释中。
 *
 * 语法：
 * - `<!-- @{directiveName} {params...} -->`
 *
 * 分类：
 * - 内联指令（inline）：如 include。默认不移除紧随其后的换行。
 * - 块指令（block）：如 if。默认移除紧随其后的换行。
 *
 * @property name 指令名称（不含 `@` 前缀）。
 * @property isBlock 是否为块指令。块指令会默认移除指令注释后的紧邻换行。
 */
interface PromptTemplateDirective {
    val name: String

    val isBlock: Boolean

    /**
     * 是否移除指令注释后的紧邻换行。
     *
     * 注意：该行为与 [isBlock] 不再强绑定，未来个别指令可自定义此行为。
     */
    val removeFollowingNewline: Boolean

    /**
     * 判断注释内容是否匹配当前指令。
     * 传入的字符串已为注释内文本并去除了首尾空白，例如："@include path.md" / "@if !flag"。
     */
    fun matches(trimmedComment: String): Boolean
}
