package icu.windea.pls.config.configExpression

import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.toCommaDelimitedStringSet

/**
 * 本地化位置表达式。
 *
 * 用于定位定义的相关本地化。
 *
 * 语法与约定：
 * - 以 `|` 分隔参数：`<location>|<args...>`。
 * - 以 `$` 开头的参数表示从指定路径读取“名称文本”以替换占位符（支持逗号分隔多路径），映射到 [namePaths]。
 * - 参数 `u` 表示将最终名称强制转为大写（[forceUpperCase]）。仅限使用占位符时有效。
 * - 当 [location] 含 `$` 时表示存在占位符，需要在后续步骤以“定义名或属性值”等替换。
 *
 * CWTools 兼容性：兼容，但存在一些扩展。
 *
 * 示例：
 * ```cwt
 * desc = "$_desc" # 用当前定义的名字替换占位符，解析为本地化的名字。
 * desc = "$_desc|$name" # 在前者基础上，改为用指定路径（`name`）的属性值替换占位符（多路径逗号分隔）。
 * desc = "$_desc|$name|u" # 在前者基础上，强制解析为大写的本地化的名字。
 * title = "title" # 得到当前定义声明中指定路径（`title`）的属性的值，解析为本地化的名字。
 * ```
 *
 * @property namePaths 用于获取名字文本的一组成员路径。名字文本用于替换占位符。
 * @property forceUpperCase 本地化的名字是否需要强制大写。
 *
 * @see icu.windea.pls.config.config.delegated.CwtTypeLocalisationConfig
 */
interface CwtLocalisationLocationExpression : CwtLocationExpression {
    val namePaths: Set<String>
    val forceUpperCase: Boolean

    operator fun component3() = namePaths
    operator fun component4() = forceUpperCase

    interface Resolver {
        fun resolveEmpty(): CwtLocalisationLocationExpression
        fun resolve(expressionString: String): CwtLocalisationLocationExpression
    }

    companion object : Resolver by CwtLocalisationLocationExpressionResolverImpl()
}

// region Implementations

private class CwtLocalisationLocationExpressionResolverImpl : CwtLocalisationLocationExpression.Resolver {
    private val cache = CacheBuilder("expireAfterAccess=30m")
        .build<String, CwtLocalisationLocationExpression> { doResolve(it) }

    private val emptyExpression = CwtLocalisationLocationExpressionImpl("", "")

    override fun resolveEmpty(): CwtLocalisationLocationExpression = emptyExpression

    override fun resolve(expressionString: String): CwtLocalisationLocationExpression {
        if (expressionString.isEmpty()) return emptyExpression
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String): CwtLocalisationLocationExpression {
        // 以 '|' 切分：首段为 location，其余为参数
        val tokens = expressionString.split('|')
        if (tokens.size == 1) return CwtLocalisationLocationExpressionImpl(expressionString, expressionString)
        val location = tokens.first()
        val args = tokens.drop(1)
        var namePaths: Set<String>? = null
        var forceUpperCase = false
        args.forEach { arg ->
            // 以 '$' 开头：表示 namePaths；参数 'u' 表示强制大写
            if (arg.startsWith('$')) {
                namePaths = arg.drop(1).toCommaDelimitedStringSet()
            } else if (arg == "u") {
                forceUpperCase = true
            }
        }
        return CwtLocalisationLocationExpressionImpl(expressionString, location, namePaths.orEmpty(), forceUpperCase)
    }
}

private class CwtLocalisationLocationExpressionImpl(
    override val expressionString: String,
    override val location: String,
    override val namePaths: Set<String> = emptySet(),
    override val forceUpperCase: Boolean = false,
) : CwtLocalisationLocationExpression {
    // 当 location 包含占位符 '$' 时，需要在后续步骤以名称文本替换
    override val isPlaceholder: Boolean = location.contains('$')

    override fun equals(other: Any?) = this === other || other is CwtLocalisationLocationExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}

// endregion
