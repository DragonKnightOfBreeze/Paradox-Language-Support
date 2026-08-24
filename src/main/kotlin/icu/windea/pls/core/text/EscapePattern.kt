package icu.windea.pls.core.text

/**
 * 转义模式。
 *
 * 用于按照特定的方式转义文本或反转义文本。
 * 转义与反转义行为不一定是完全相反的。例如，一段文本在经由转义后再反转义，结果可以不等同于原始内容。
 *
 * 作为对转义逻辑的策略。
 *
 * @see EscapePatterns
 */
interface EscapePattern {
    /** 转义 [text]，将其中的特定字符串替换为其转义形式。 */
    fun escape(text: String): String

    /** 反转义 [text]，将其中的特定字符串替换为其普通形式。 */
    fun unescape(text: String): String

    abstract class Base : EscapePattern
}
