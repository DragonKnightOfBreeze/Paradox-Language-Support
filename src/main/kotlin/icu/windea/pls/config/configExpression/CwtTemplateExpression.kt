package icu.windea.pls.config.configExpression

import icu.windea.pls.config.CwtDataTypes.Constant
import icu.windea.pls.config.CwtDataTypes.Definition
import icu.windea.pls.config.CwtDataTypes.EnumValue
import icu.windea.pls.config.CwtDataTypes.Icon
import icu.windea.pls.config.CwtDataTypes.Scope
import icu.windea.pls.config.CwtDataTypes.Value
import icu.windea.pls.config.configExpression.impl.CwtTemplateExpressionResolverImpl

/**
 * CWT 模板表达式。
 *
 * 用于描述脚本文件中的键或值的更复杂的取值形态，可视为多个数据表达式的组合。
 * 由数个片段拼接而成：常量字段 + 动态片段（受限支持的数据表达式）。
 *
 * 说明：
 * - 不允许包含空白字符；包含空白将直接返回空表达式。
 * - 采用“最左最早匹配”的方式，基于所有动态规则（具有前后缀的规则）扫描字符串，拆分出常量/动态片段。
 * - 仅存在一个片段（纯常量或纯动态）时视为不构成模板，返回空表达式。
 * - 常量片段中若出现“非标识符字符 + 常量规则名”的组合，会进行一次拆分以规避误判（见实现中的注释：`#129`）。
 *
 * 适用对象：定义成员对应的规则的键或值。
 *
 * CWTools 兼容性：兼容，但实际的解析与处理逻辑可能不同。
 *
 * 示例：
 *
 * ```cwt
 * 	<planet_class>_habitability # definition + constant
 * 	enum[ship_class]_armor_mult # complex enum + constant
 * ```
 *
 * @property snippetExpressions 解析得到的所有片段，顺序与原始字符串一致。常量片段类型为 `[Constant]`，
 * 动态片段类型取决于匹配到的规则（如 [Value]、[EnumValue]、[Scope]、[Icon]、[Definition] 等）。
 * @property referenceExpressions 过滤后的“引用片段”（即非 [Constant] 片段），用于后续的引用解析、导航与高亮。
 *
 * @see CwtDataExpression
 * @see icu.windea.pls.ep.configExpression.CwtDataExpressionResolver
 */
interface CwtTemplateExpression : CwtConfigExpression {
    val snippetExpressions: List<CwtDataExpression>
    val referenceExpressions: List<CwtDataExpression>

    interface Resolver {
        fun resolveEmpty(): CwtTemplateExpression
        fun resolve(expressionString: String): CwtTemplateExpression
    }

    companion object : Resolver by CwtTemplateExpressionResolverImpl()
}
