package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxScriptValueExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueNode

/**
 * 脚本值表达式。
 *
 * 说明：
 * - 作为 [ParadoxValueFieldExpression] 的一部分。
 *
 * 示例：
 * ```
 * some_sv
 * some_sv|PARAM|VALUE|
 * ```
 *
 * 语法：
 * ```bnf
 * script_value_expression ::= script_value script_value_args?
 * private script_value_args ::= "|" (script_value_argument "|" script_value_argument_value "|")+
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 以 `|` 为分隔，将文本拆分为：脚本值名、后续若干“参数名/参数值”对。
 * - 允许仅有脚本值名，或脚本值名后跟一个或多个成对出现的参数（`|name|value|`）。
 *
 * #### 节点组成
 * - 脚本值名：[ParadoxScriptValueNode]（第 1 段）。
 * - 参数名/参数值：成对出现，分别为 [ParadoxScriptValueArgumentNode] 与 [ParadoxScriptValueArgumentValueNode]（后续各段）。
 *
 * #### 解析与约束
 * - 参数名需为合法标识符；脚本值名需为可识别的标识符（允许参数变量）。
 * - 管道数量应为 0 或奇数（≥3）。仅 1 个或非零偶数个 `|` 将被视为格式错误。
 */
interface ParadoxScriptValueExpression : ParadoxComplexExpression {
    val config: CwtConfig<*>

    val scriptValueNode: ParadoxScriptValueNode
    val argumentNodes: List<Pair<ParadoxScriptValueArgumentNode, ParadoxScriptValueArgumentValueNode?>>

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxScriptValueExpression?
    }

    companion object : Resolver by ParadoxScriptValueExpressionResolverImpl()
}
