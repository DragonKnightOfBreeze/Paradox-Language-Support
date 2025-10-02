package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxDynamicValueExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode

/**
 * 动态值表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypeGroups.DynamicValue]。
 *
 * 示例：
 * ```
 * some_variable
 * some_variable@root
 * ```
 *
 * 语法：
 * ```bnf
 * dynamic_value_expression ::= dynamic_value ("@" scope_field_expression)?
 * dynamic_value ::= TOKEN // matching config expression "value[xxx]" or "value_set[xxx]"
 * // "event_target:t1.v1@event_target:t2.v2@..." is not used in vanilla files but allowed here
 * ```
 */
interface ParadoxDynamicValueExpression : ParadoxComplexExpression {
    val configs: List<CwtConfig<*>>

    val dynamicValueNode: ParadoxDynamicValueNode
    val scopeFieldExpression: ParadoxScopeFieldExpression?

    interface Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxDynamicValueExpression?
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression?
    }

    companion object : Resolver by ParadoxDynamicValueExpressionResolverImpl()
}
