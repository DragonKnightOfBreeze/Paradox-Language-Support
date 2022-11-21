package icu.windea.pls.script.exp

import icu.windea.pls.script.exp.nodes.*

/**
 * 值字段表达式。
 *
 * 语法：
 *
 * ```bnf
 * value_field_expression ::= scope * value_field
 * scope ::= system_scope | scope_link | scope_link_from_data
 * system_scope ::= TOKEN //predefined by Internal Config (in script_config.pls.cwt)
 * scope_link ::= TOKEN //predefined by CWT Config (in links.cwt)
 * scope_link_from_data ::= scope_link_prefix scope_link_data_source //predefined by CWT Config (in links.cwt)
 * scope_link_prefix ::= TOKEN //e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_data_source ::= expression //e.g. "some_variable" while the link's data source is "value[variable]"
 * value_field ::= value_link | value_link_from_data
 * value_link ::= TOKEN //predefined by CWT Config (in links.cwt, from_data = false, type = both | value)
 * value_link_from_data ::= value_link_prefix value_link_data_source //predefined by CWT Config (in links.cwt, from_data = true, type = both | value)
 * value_link_prefix ::= TOKEN //e.g. "value:" while the link's prefix is "value:"
 * value_link_data_source ::= expression //e.g. "some" while the link's data source is "value[variable]"
 * expression ::= data_expression | value_set_value_expression | sv_expression //see: ParadoxDataExpression, ParadoxValueSetValueExpression
 * sv_expression ::= sv_name ("|" (param_name "|" param_value "|")+)? //e.g. value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * ```
 *
 * 示例：
 *
 * ```
 * trigger:some_trigger
 * value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * root.owner.some_variable
 * ```
 */
interface ParadoxValueFieldExpression: ParadoxScriptComplexExpression{
	val scopes: List<ParadoxScopeExpressionNode>
	
	companion object Resolver
}
