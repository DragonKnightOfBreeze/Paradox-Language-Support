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
 * script_value_expression ::= script_value ("|" (arg_name "|" arg_value "|")+)?
 * script_value ::= TOKEN // matching config expression "<script_value>"
 * arg_name ::= TOKEN // argument name, no surrounding "$"
 * arg_value ::= TOKEN // boolean, int, float or string
 * ```
 */
interface ParadoxScriptValueExpression : ParadoxComplexExpression {
    val config: CwtConfig<*>

    val scriptValueNode: ParadoxScriptValueNode
    val argumentNodes: List<Pair<ParadoxScriptValueArgumentNode, ParadoxScriptValueArgumentValueNode?>>

    interface Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxScriptValueExpression?
    }

    companion object : Resolver by ParadoxScriptValueExpressionResolverImpl()
}
