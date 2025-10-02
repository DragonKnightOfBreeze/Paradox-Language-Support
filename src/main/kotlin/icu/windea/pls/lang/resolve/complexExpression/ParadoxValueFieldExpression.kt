package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxValueFieldExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldNode

/**
 * 值字段表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypeGroups.ValueField]。
 *
 * 示例：
 * ```
 * trigger:some_trigger
 * value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * root.owner.some_variable
 * ```
 *
 * 语法：
 * ```bnf
 * value_field_expression ::= scope * value_field
 * scope ::= system_scope | scope_link | scope_link_from_data
 * system_scope ::= TOKEN // predefined by CWT Config (see system_scopes.cwt)
 * scope_link ::= TOKEN // predefined by CWT Config (see links.cwt)
 * scope_link_from_data ::= scope_link_prefix scope_link_value // predefined by CWT Config (see links.cwt)
 * scope_link_prefix ::= TOKEN // e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_value ::= expression // e.g. "some_variable" while the link's data source is "value[variable]"
 * value_field ::= value_link | value_link_from_data
 * value_link ::= TOKEN // predefined by CWT Config (see links.cwt)
 * value_link_from_data ::= value_field_prefix value_field_value // predefined by CWT Config (see links.cwt)
 * value_field_prefix ::= TOKEN // e.g. "value:" while the link's prefix is "value:"
 * value_field_value ::= expression // e.g. "some_variable" while the link's data source is "value[variable]"
 * expression ::= data_source | dynamic_value_expression | sv_expression // see: ParadoxDataSourceNode, ParadoxDynamicValueExpression
 * sv_expression ::= sv_name ("|" (param_name "|" param_value "|")+)? // e.g. value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * ```
 */
interface ParadoxValueFieldExpression : ParadoxComplexExpression {
    val scopeNodes: List<ParadoxScopeLinkNode>
    val valueFieldNode: ParadoxValueFieldNode
    val scriptValueExpression: ParadoxScriptValueExpression?

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldExpression?
    }

    companion object : Resolver by ParadoxValueFieldExpressionResolverImpl()
}
