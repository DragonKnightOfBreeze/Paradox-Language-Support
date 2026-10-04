package icu.windea.pls.core.util

import com.github.benmanes.caffeine.cache.Interner

/**
 * 模板信息。
 *
 * 表示一个带占位符的模板。给定占位符对应的实参，可以解析出完整文本。反之，也可以从完整文本中提取出对应的实参。
 *
 * @property expression 表达式字符串。
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
 * 一元模板信息。
 *
 * 表示一个仅包含单个占位符的模板。例如 `$_suffix` `prefix_$` `prefix_$_suffix`。
 *
 * @property expression 表达式字符串。要求仅包含单个占位符。
 * @property placeholder 占位符。默认为 `$`。
 * @property prefix 占位符之前的常量前缀。
 * @property suffix 占位符之后的常量后缀。
 */
data class UnaryTemplateInfo(
    override val expression: String,
    val placeholder: String = "$",
) : TemplateInfo<String> {
    val prefix: String = expression.substringBefore(placeholder)
    val suffix: String = expression.substringAfter(placeholder)

    override fun extract(text: String): String? {
        if (text.length < prefix.length + suffix.length) return null
        if (!text.startsWith(prefix)) return null
        if (!text.endsWith(suffix)) return null
        return text.substring(prefix.length, text.length - suffix.length)
    }

    override fun resolve(args: String): String {
        return prefix + args + suffix
    }

    override fun toString(): String = expression

    /**
     * 进行规范化处理（去重）。
     */
    fun normalize(): UnaryTemplateInfo {
        return interner.intern(this)
    }

    companion object {
        private val interner = Interner.newWeakInterner<UnaryTemplateInfo>()

        /**
         * 从表达式字符串直接解析并创建实例。
         *
         * 如果表达式为空或不包含唯一的占位符，则返回 `null`。
         */
        @JvmStatic
        fun create(expression: String, placeholder: String = "$"): UnaryTemplateInfo? {
            if (expression.isEmpty()) return null
            val index = expression.indexOf(placeholder)
            if (index == -1) return null // require placeholder
            val nextIndex = expression.indexOf(placeholder, index + 1)
            if (nextIndex != -1) return null // require exactly one placeholder
            return UnaryTemplateInfo(expression)
        }

        // TODO 3.0.4 [snippet-match] remove
        @JvmStatic
        fun from(expression: String, placeholder: String = "$"): UnaryTemplateInfo? {
            return create(expression, placeholder)?.normalize()
        }
    }
}
