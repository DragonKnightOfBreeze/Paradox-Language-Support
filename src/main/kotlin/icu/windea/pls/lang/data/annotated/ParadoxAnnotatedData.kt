package icu.windea.pls.lang.data.annotated

/**
 * 注解数据。
 *
 * 用于提取、嵌入、渲染或检查特定的语法信息和语义信息，例如类型和规则表达式。
 * 可以被渲染为文本或者特殊注释。
 *
 * @see ParadoxAnnotatedInfoFactory
 * @see ParadoxAnnotatedLevel
 */
interface ParadoxAnnotatedData {
    val name: String

    fun render(): String

    fun toComment(): String

    abstract class Base(override val name: String) : ParadoxAnnotatedData {
        // 3.0.2 `QuotePatterns.Default` should be used here to quote literal if needed
        abstract override fun render(): String

        override fun toComment(): String {
            return "## @$name ${render()}"
        }
    }
}
