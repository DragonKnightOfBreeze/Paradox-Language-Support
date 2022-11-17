package icu.windea.pls.script.exp

/**
 * 作用域字段表达式。
 * 
 * 语法：
 * 
 * ```bnf
 * scope_field_expression ::= scope_expression +
 * scope_expression ::= system_scope | scope_link | scope_link_from_data
 * system_scope ::= TOKEN //predefined by Internal Config (in script_config.pls.cwt)
 * scope_link ::= TOKEN //predefined by CWT Config (in links.cwt)
 * scope_link_from_data ::= prefix data_source //predefined by CWT Config (in links.cwt)
 * prefix ::= TOKEN //e.g. "event_target:" while the link's prefix is "event_target:"
 * data_source ::= EXPRESSION //e.g. "some_variable" while the link's data source is "value[variable]"
 * ```
 * 
 * 示例：
 * 
 * ```
 * root
 * root.owner
 * event_target:some_target
 * ```
 */
class ParadoxScopeFieldExpression {
	
}