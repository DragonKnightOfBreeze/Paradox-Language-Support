package icu.windea.pls.config.configExpression.impl

import com.google.common.cache.CacheBuilder
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.core.toCommaDelimitedStringSet
import icu.windea.pls.core.util.buildCache
import java.util.concurrent.TimeUnit

internal class CwtImageLocationExpressionResolverImpl : CwtImageLocationExpression.Resolver {
    // 解析结果缓存：图像路径表达式在索引/渲染等流程中会被反复解析
    // - maximumSize: 限制缓存容量，防止内存无限增长
    // - expireAfterAccess: 非热点表达式在一段时间未被访问后回收
    private val cache = CacheBuilder.newBuilder()
        .maximumSize(4096)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .buildCache<String, CwtImageLocationExpression> { doResolve(it) }

    private val emptyExpression = CwtImageLocationExpressionImpl("", "")

    override fun resolveEmpty(): CwtImageLocationExpression = emptyExpression

    override fun resolve(expressionString: String): CwtImageLocationExpression {
        if (expressionString.isEmpty()) return emptyExpression
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String): CwtImageLocationExpression {
        // 以 '|' 切分：首段为 location，其余为参数
        val tokens = expressionString.split('|')
        // 仅包含 location，无额外参数
        if (tokens.size == 1) return CwtImageLocationExpressionImpl(expressionString, expressionString)
        val location = tokens.first()
        val args = tokens.drop(1)
        var namePaths: Set<String>? = null
        var framePaths: Set<String>? = null
        args.forEach { arg ->
            // 以 '$' 开头：表示 namePaths；否则为 framePaths
            // 若出现多次，同类参数以后者覆盖（按实现顺序）
            if (arg.startsWith('$')) {
                namePaths = arg.drop(1).toCommaDelimitedStringSet()
            } else {
                framePaths = arg.toCommaDelimitedStringSet()
            }
        }
        return CwtImageLocationExpressionImpl(expressionString, location, namePaths.orEmpty(), framePaths.orEmpty())
    }
}

private class CwtImageLocationExpressionImpl(
    override val expressionString: String,
    override val location: String,
    override val namePaths: Set<String> = emptySet(),
    override val framePaths: Set<String> = emptySet(),
) : CwtImageLocationExpression {
    // 当 location 包含占位符 '$' 时，需要在后续步骤以名称文本替换
    override val isPlaceholder: Boolean = location.contains('$')

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtImageLocationExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}
