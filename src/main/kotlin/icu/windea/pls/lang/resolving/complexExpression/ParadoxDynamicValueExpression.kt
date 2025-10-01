package icu.windea.pls.lang.resolving.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolving.complexExpression.impl.ParadoxDynamicValueExpressionResolverImpl
import icu.windea.pls.lang.resolving.complexExpression.nodes.ParadoxDynamicValueNode

/**
 * 动态值表达式。对应的规则类型为 [icu.windea.pls.config.CwtDataTypeGroups.DynamicValue]。
 *
 * 语法：
 * ```bnf
 * dynamic_value_expression ::= dynamic_value ("@" scope_field_expression)?
 * dynamic_value ::= TOKEN // matching config expression "value[xxx]" or "value_set[xxx]"
 * // "event_target:t1.v1@event_target:t2.v2@..." is not used in vanilla files but allowed here
 * ```
 *
 * 示例：
 * ```
 * some_variable
 * some_variable@root
 * ```
 *
 * @see icu.windea.pls.config.CwtDataTypeGroups.DynamicValue
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
