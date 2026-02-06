package icu.windea.pls.config.configExpression

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.indicesOf
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.removeSurroundingOrNull

/**
 * 模式表达式。
 *
 * 用于描述规则文件中的键与值的取值形态，从而为规则文件本身提供代码补全、代码检查等功能。
 * 目前仅用于提供基础的代码补全，且仅在 `cwt/core/schema.cwt` 中有用到。
 *
 * 支持的形态：
 * - 常量（[Constant]）：不包含 `$` 的原样字符串。
 * - 模板（[Template]）：包含一个或多个参数（`$...$`），如：`$type$`、`type[$type$]`。
 * - 类型（[Type]）：以单个 `$` 起始，如：`$any`、`$int`。
 * - 约束（[Constraint]）：以 `$$` 起始，如：`$$declaration`。
 * - 枚举（[Enum]）：以 `$enum:` 起始并以 `$` 结尾，如：`$enum:ship_size$`。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * ## cardinality = 0..1
 * ## cardinality = 0..inf
 * ## cardinality = ~1..10
 * ```
 *
 * @see icu.windea.pls.config.config.internal.CwtSchemaConfig
 * @see icu.windea.pls.config.util.CwtConfigManager
 * @see icu.windea.pls.lang.codeInsight.completion.CwtConfigCompletionManager
 */
interface CwtSchemaExpression : CwtConfigExpression {
    interface Constant : CwtSchemaExpression

    interface Template : CwtSchemaExpression {
        val pattern: String
        val parameterRanges: List<TextRange>
    }

    interface Type : CwtSchemaExpression {
        val name: String
    }

    interface Enum : CwtSchemaExpression {
        val name: String
    }

    interface Constraint : CwtSchemaExpression {
        val name: String
    }

    interface Resolver {
        fun resolveEmpty(): CwtSchemaExpression
        fun resolve(expressionString: String): CwtSchemaExpression
    }

    companion object : Resolver by CwtSchemaExpressionResolverImpl()
}

// region Implementations

private class CwtSchemaExpressionResolverImpl : CwtSchemaExpression.Resolver {
    private val logger = thisLogger()
    private val cache = CacheBuilder("expireAfterAccess=30m")
        .build<String, CwtSchemaExpression> { doResolve(it) }

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

private sealed class CwtSchemaExpressionBase : CwtSchemaExpression {
    override fun equals(other: Any?) = this === other || other is CwtSchemaExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}

private class CwtSchemaConstantExpression(
    override val expressionString: String
) : CwtSchemaExpressionBase(), CwtSchemaExpression.Constant

private class CwtSchemaTemplateExpression(
    override val expressionString: String,
    override val pattern: String,
    override val parameterRanges: List<TextRange>
) : CwtSchemaExpressionBase(), CwtSchemaExpression.Template

private class CwtSchemaTypeExpression(
    override val expressionString: String,
    override val name: String
) : CwtSchemaExpressionBase(), CwtSchemaExpression.Type

private class CwtSchemaEnumExpression(
    override val expressionString: String,
    override val name: String
) : CwtSchemaExpressionBase(), CwtSchemaExpression.Enum

private class CwtSchemaConstraintExpression(
    override val expressionString: String,
    override val name: String
) : CwtSchemaExpressionBase(), CwtSchemaExpression.Constraint

// endregion
