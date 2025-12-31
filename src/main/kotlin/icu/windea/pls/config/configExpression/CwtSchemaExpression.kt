package icu.windea.pls.config.configExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configExpression.impl.CwtSchemaExpressionResolverImpl

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
