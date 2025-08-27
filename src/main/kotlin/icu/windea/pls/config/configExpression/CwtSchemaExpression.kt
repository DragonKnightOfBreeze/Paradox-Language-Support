@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.configExpression

import com.google.common.cache.CacheBuilder
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.indicesOf
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.buildCache

private val logger = logger<CwtSchemaExpression>()

sealed class CwtSchemaExpression(
    override val expressionString: String
) : CwtConfigExpression {
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtSchemaExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int {
        return expressionString.hashCode()
    }

    override fun toString(): String {
        return expressionString
    }

    class Constant(
        expressionString: String
    ) : CwtSchemaExpression(expressionString)

    class Template(
        expressionString: String,
        val pattern: String,
        val parameterRanges: List<TextRange>
    ) : CwtSchemaExpression(expressionString)

    class Type(
        expressionString: String,
        val name: String
    ) : CwtSchemaExpression(expressionString)

    class Enum(
        expressionString: String,
        val name: String
    ) : CwtSchemaExpression(expressionString)

    class Constraint(
        expressionString: String,
        val name: String
    ) : CwtSchemaExpression(expressionString)

    companion object Resolver {
        private val cache = CacheBuilder.newBuilder().buildCache<String, CwtSchemaExpression> { doResolve(it) }
        private val parameterRegex = """(?<!\\)\$.*?\$""".toRegex()

        fun resolve(expressionString: String): CwtSchemaExpression = cache.get(expressionString)

        private fun doResolve(expressionString: String): CwtSchemaExpression {
            val indices = expressionString.indicesOf('$')
            if (indices.isEmpty()) {
                return Constant(expressionString)
            }
            if (indices.size == 1) {
                run {
                    val name = expressionString.removePrefixOrNull("$") ?: return@run
                    return Type(expressionString, name)
                }
            } else if (indices.size == 2) {
                run {
                    val name = expressionString.removePrefixOrNull("$$") ?: return@run
                    return Constraint(expressionString, name)
                }
                run {
                    val name = expressionString.removeSurroundingOrNull("\$enum:", "$") ?: return@run
                    return Enum(expressionString, name)
                }
            }
            if (indices.size % 2 == 1) {
                logger.warn("Invalid schema expression $expressionString, fallback to constant")
                return Constant(expressionString)
            }
            val pattern = expressionString.replace(parameterRegex, "*")
            val parameterRanges = indices
                .windowed(2, 2) { (i1, i2) -> TextRange.create(i1, i2 + 1) }
            return Template(expressionString, pattern, parameterRanges)
        }
    }
}
