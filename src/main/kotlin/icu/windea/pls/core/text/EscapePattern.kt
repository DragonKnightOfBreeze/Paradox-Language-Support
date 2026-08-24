package icu.windea.pls.core.text

/**
 * 转义模式。
 *
 * 用于按照特定的方式转义文本。
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
