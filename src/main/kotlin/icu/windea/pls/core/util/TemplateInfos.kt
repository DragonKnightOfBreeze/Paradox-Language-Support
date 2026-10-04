package icu.windea.pls.core.util

import icu.windea.pls.core.cache.CacheBuilder

/**
 * 模板信息。
 *
 * 用于描述一个带占位符的模板：给定占位符对应的实参，可以解析出完整文本；反之，也可以从完整文本中提取出对应的实参。
 *
 * @property expression 模板表达式（包含占位符）。
 */
interface TemplateInfo<T> {
    val expression: String

    /**
     * 从完整文本 [text] 中提取占位符对应的实参。若 [text] 不符合模板格式，则返回 `null`。
     */
    fun extract(text: String): T?

    /**
     * 将实参 [args] 代入模板，解析出完整文本。
     */
    fun resolve(args: T): String
}

/**
 * 一元模板信息。即只包含单个占位符的模板，例如 `$_desc`、`GFX_$`。
 *
 * 说明：
 * - [prefix] 为占位符之前的常量前缀，[suffix] 为占位符之后的常量后缀。
 * - [extract] 要求 [String.startsWith] [prefix] 且 [String.endsWith] [suffix]，否则返回 `null`。
 *
 * @property placeholder 占位符（默认为 `$`）。
 */
data class UnaryTemplateInfo(
    override val expression: String,
    val placeholder: String = "$",
) : TemplateInfo<String> {
    /** 占位符之前的常量前缀。 */
    val prefix: String = expression.substringBefore(placeholder)
    /** 占位符之后的常量后缀。 */
    val suffix: String = expression.substringAfter(placeholder)

    override fun extract(text: String): String? {
        if (text.length < prefix.length + suffix.length) return null
        if (!text.startsWith(prefix)) return null
        if (!text.endsWith(suffix)) return null
        return text.substring(prefix.length, text.length - suffix.length)
    }

    override fun resolve(args: String): String = prefix + args + suffix

    override fun toString(): String = expression

    companion object {
        private val cache = CacheBuilder("maximumSize=1000").build<String, UnaryTemplateInfo>()

        /**
         * 从表达式字符串直接解析并创建实例。不对结果进行缓存。
         *
         * 若表达式为空或不包含唯一的占位符，则返回 `null`。
         */
        @JvmStatic
        fun create(expression: String): UnaryTemplateInfo? {
            if (expression.isEmpty()) return null
            val index = expression.indexOf('$')
            if (index == -1) return null
            if (expression.indexOf('$', index + 1) != -1) return null // require exactly one placeholder
            return UnaryTemplateInfo(expression)
        }

        /**
         * 从表达式字符串解析实例，并对成功解析的结果进行缓存和去重。
         */
        @JvmStatic
        fun from(expression: String): UnaryTemplateInfo? {
            cache.getIfPresent(expression)?.let { return it }
            val result = create(expression) ?: return null
            cache.put(expression, result)
            return result
        }
    }
}
