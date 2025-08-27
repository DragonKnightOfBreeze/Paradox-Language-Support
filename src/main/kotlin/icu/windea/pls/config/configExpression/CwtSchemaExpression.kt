package icu.windea.pls.config.configExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configExpression.impl.CwtSchemaExpressionResolverImpl

/**
 * CWT 架构（schema）表达式。
 *
 * 该表达式用于描述规则右侧允许的取值形态，常见于 `.cwt` 的数据类型或占位模板中。
 *
 * 支持的形态：
 * - 常量（Constant）：不包含 `$` 的原样字符串。
 * - 类型（Type）：以单个 `$` 起始，如：`$any`、`$int`。
 * - 约束（Constraint）：以 `$$` 起始，如：`$$custom`。
 * - 枚举（Enum）：以 `$enum:` 起始并以 `$` 结尾，如：`$enum:ship_size$`。
 * - 模板（Template）：包含成对的 `$...$` 片段，其他部分为常量，例如：`a $x$ b $y$`。
 *
 * 解析约定：
 * - 奇数个 `$` 将判作非法模板并回退为常量表达式。
 * - 模板形态中，所有未被转义的 `$...$` 片段会被视为参数；其在 `pattern` 中将以 `*` 占位，
 *   同时记录每个参数在原始字符串中的范围，便于二次处理与高亮。
 *
 * 参考：`references/cwt/guidance.md` 与 `docs/zh/config.md`。
 */
interface CwtSchemaExpression : CwtConfigExpression {
    /** 常量表达式。 */
    interface Constant : CwtSchemaExpression

    /**
     * 模板表达式。
     *
     * 例如：`a $x$ b $y$`。
     * - `pattern` 值为 `a * b *`，用于匹配/展示。
     * - `parameterRanges` 保存每个 `$...$` 片段在原始字符串中的范围（闭开区间）。
     */
    interface Template : CwtSchemaExpression {
        val pattern: String
        val parameterRanges: List<TextRange>
    }

    /** 类型表达式，如 `$any`。*/
    interface Type : CwtSchemaExpression {
        val name: String
    }

    /** 枚举表达式，如 `$enum:ship_size$`。*/
    interface Enum : CwtSchemaExpression {
        val name: String
    }

    /** 约束表达式，如 `$$custom`。*/
    interface Constraint : CwtSchemaExpression {
        val name: String
    }

    interface Resolver {
        fun resolveEmpty(): CwtSchemaExpression
        fun resolve(expressionString: String): CwtSchemaExpression
    }

    companion object : Resolver by CwtSchemaExpressionResolverImpl()
}
