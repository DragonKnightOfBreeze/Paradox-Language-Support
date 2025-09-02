package icu.windea.pls.config.configExpression.impl

import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.core.toCommaDelimitedStringSet
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.core.util.cancelable

internal class CwtLocalisationLocationExpressionResolverImpl : CwtLocalisationLocationExpression.Resolver {
    // 解析结果缓存：本地化路径表达式在补全/导航中被频繁使用
    // - maximumSize: 限制缓存容量，防止内存无限增长
    // - expireAfterAccess: 非热点表达式在一段时间未被访问后回收
    private val cache = CacheBuilder("maximumSize=4096, expireAfterAccess=10m")
        .build<String, CwtLocalisationLocationExpression> { doResolve(it) }
        .cancelable()

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

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtLocalisationLocationExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}
