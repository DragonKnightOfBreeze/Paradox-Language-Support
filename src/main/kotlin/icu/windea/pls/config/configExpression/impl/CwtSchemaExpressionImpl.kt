package icu.windea.pls.config.configExpression.impl

import com.google.common.cache.CacheBuilder
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configExpression.CwtSchemaExpression
import icu.windea.pls.core.indicesOf
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.buildCache

internal class CwtSchemaExpressionResolverImpl : CwtSchemaExpression.Resolver {
    private val logger = logger<CwtSchemaExpression>()

    private val cache = CacheBuilder.newBuilder().buildCache<String, CwtSchemaExpression> { doResolve(it) }
    private val parameterRegex = """(?<!\\)\$.*?\$""".toRegex()

    private val emptyExpression: CwtSchemaExpression = CwtSchemaConstantExpression("")

    override fun resolveEmpty(): CwtSchemaExpression = emptyExpression

    override fun resolve(expressionString: String): CwtSchemaExpression {
        if (expressionString.isEmpty()) return emptyExpression
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String): CwtSchemaExpression {
        val indices = expressionString.indicesOf('$')
        if (indices.isEmpty()) {
            return CwtSchemaConstantExpression(expressionString)
        }
        if (indices.size == 1) {
            run {
                val name = expressionString.removePrefixOrNull("$") ?: return@run
                return CwtSchemaTypeExpression(expressionString, name)
            }
        } else if (indices.size == 2) {
            run {
                val name = expressionString.removePrefixOrNull("$$") ?: return@run
                return CwtSchemaConstraintExpression(expressionString, name)
            }
            run {
                val name = expressionString.removeSurroundingOrNull("\$enum:", "$") ?: return@run
                return CwtSchemaEnumExpression(expressionString, name)
            }
        }
        if (indices.size % 2 == 1) {
            logger.warn("Invalid schema expression $expressionString, fallback to constant")
            return CwtSchemaConstantExpression(expressionString)
        }
        val pattern = expressionString.replace(parameterRegex, "*")
        val parameterRanges = indices
            .windowed(2, 2) { (i1, i2) -> TextRange.create(i1, i2 + 1) }
        return CwtSchemaTemplateExpression(expressionString, pattern, parameterRanges)
    }
}

private class CwtSchemaConstantExpression(
    override val expressionString: String
) : CwtSchemaExpression.Constant {
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtSchemaExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}

private class CwtSchemaTemplateExpression(
    override val expressionString: String,
    override val pattern: String,
    override val parameterRanges: List<TextRange>
) : CwtSchemaExpression.Template {
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtSchemaExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}

private class CwtSchemaTypeExpression(
    override val expressionString: String,
    override val name: String
) : CwtSchemaExpression.Type {
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtSchemaExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}

private class CwtSchemaEnumExpression(
    override val expressionString: String,
    override val name: String
) : CwtSchemaExpression.Enum {
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtSchemaExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}

private class CwtSchemaConstraintExpression(
    override val expressionString: String,
    override val name: String
) : CwtSchemaExpression.Constraint {
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtSchemaExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}
