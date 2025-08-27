package icu.windea.pls.config.configExpression.impl

import com.google.common.cache.CacheBuilder
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.core.toCommaDelimitedStringSet
import icu.windea.pls.core.util.buildCache

internal class CwtImageLocationExpressionResolverImpl : CwtImageLocationExpression.Resolver {
    private val cache = CacheBuilder.newBuilder().buildCache<String, CwtImageLocationExpression> { doResolve(it) }
    private val emptyExpression = CwtImageLocationExpressionImpl("", "")

    override fun resolveEmpty(): CwtImageLocationExpression = emptyExpression

    override fun resolve(expressionString: String): CwtImageLocationExpression {
        if (expressionString.isEmpty()) return emptyExpression
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String): CwtImageLocationExpression {
        if (expressionString.isEmpty()) return emptyExpression
        val tokens = expressionString.split('|')
        if (tokens.size == 1) return CwtImageLocationExpressionImpl(expressionString, expressionString)
        val location = tokens.first()
        val args = tokens.drop(1)
        var namePaths: Set<String>? = null
        var framePaths: Set<String>? = null
        args.forEach { arg ->
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
    override val isPlaceholder: Boolean = location.contains('$')

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtImageLocationExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}
