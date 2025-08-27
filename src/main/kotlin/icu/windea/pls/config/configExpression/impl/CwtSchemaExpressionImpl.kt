package icu.windea.pls.config.configExpression.impl

import com.google.common.cache.CacheBuilder
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configExpression.CwtSchemaExpression
import icu.windea.pls.core.indicesOf
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.buildCache

internal class CwtSchemaExpressionResolverImpl : CwtSchemaExpression.Resolver {
    private val logger = thisLogger()
    private val cache = CacheBuilder.newBuilder().buildCache<String, CwtSchemaExpression> { doResolve(it) }

    // 匹配未转义的 `$...$` 片段，用于生成模板的 pattern（替换为 `*`）
    private val parameterRegex = """(?<!\\)\$.*?\$""".toRegex()

    private val emptyExpression: CwtSchemaExpression = CwtSchemaConstantExpression("")

    override fun resolveEmpty(): CwtSchemaExpression = emptyExpression

    override fun resolve(expressionString: String): CwtSchemaExpression {
        if (expressionString.isEmpty()) return emptyExpression
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String): CwtSchemaExpression {
        // 收集所有 `$` 的索引，以便快速判定表达式形态
        val indices = expressionString.indicesOf('$').filter { !expressionString.isEscapedCharAt(it) }
        if (indices.isEmpty()) {
            return CwtSchemaConstantExpression(expressionString)
        }
        if (indices.size == 1) {
            run {
                // `$xxx` -> 类型表达式
                val name = expressionString.removePrefixOrNull("$") ?: return@run
                return CwtSchemaTypeExpression(expressionString, name)
            }
        } else if (indices.size == 2) {
            run {
                // `$$xxx` -> 约束表达式
                val name = expressionString.removePrefixOrNull("$$") ?: return@run
                return CwtSchemaConstraintExpression(expressionString, name)
            }
            run {
                // `$enum:xxx$` -> 枚举表达式
                val name = expressionString.removeSurroundingOrNull("\$enum:", "$") ?: return@run
                return CwtSchemaEnumExpression(expressionString, name)
            }
        }
        // 奇数个 `$` 视为非法模板，回退为常量（保持容错）
        if (indices.size % 2 == 1) {
            logger.warn("Invalid schema expression $expressionString, fallback to constant")
            return CwtSchemaConstantExpression(expressionString)
        }
        // 模板：将 `$...$` 片段替换为 `*` 形成展示/匹配用的 pattern
        val pattern = expressionString.replace(parameterRegex, "*")
        val parameterRanges = indices
            .windowed(2, 2) { (i1, i2) -> TextRange.create(i1, i2 + 1) }
        return CwtSchemaTemplateExpression(expressionString, pattern, parameterRanges)
    }
}

private sealed class CwtSchemaExpressionImplBase : CwtSchemaExpression {
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtSchemaExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}

private class CwtSchemaConstantExpression(
    override val expressionString: String
) : CwtSchemaExpressionImplBase(), CwtSchemaExpression.Constant

private class CwtSchemaTemplateExpression(
    override val expressionString: String,
    override val pattern: String,
    override val parameterRanges: List<TextRange>
) : CwtSchemaExpressionImplBase(), CwtSchemaExpression.Template

private class CwtSchemaTypeExpression(
    override val expressionString: String,
    override val name: String
) : CwtSchemaExpressionImplBase(), CwtSchemaExpression.Type

private class CwtSchemaEnumExpression(
    override val expressionString: String,
    override val name: String
) : CwtSchemaExpressionImplBase(), CwtSchemaExpression.Enum

private class CwtSchemaConstraintExpression(
    override val expressionString: String,
    override val name: String
) : CwtSchemaExpressionImplBase(), CwtSchemaExpression.Constraint
