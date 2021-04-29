package com.windea.plugin.idea.pls.model

import com.windea.plugin.idea.pls.*

/**
 * There are a number of options that can be applied to rules by placing them in a comment above the rule
 * 
 * @property cardinality `## cardinality = 0..1`: the following rule can be matched between `min..max` times, where min is any integer and max is any integer or `inf` for unlimited.
 * @property severity `## severity = information`: the following rule has any errors changed to this severity (error/warning/information/hint)
 * @property push_scope `## push_scope = country`: the following rule is a context switch/scope change into the specified scope. This adds the scope onto the current scope stack.
 * @property replace_scope `## replace_scope = { this = planet root = planet from = ship fromfrom = country }`: this following rule replaces the given parts of the current scope context with the given scopes. Any number of `this`,`root`,`from`,`fromfrom`,`fromfromfrom` and `fromfromfromfrom` can be specified.
 * @property scopes `## scope = country`: the following rule is only valid when the current scope matches that given. Alternatively `## scope = { country planet }` for multiple valid scopes.
 */
class CwtConfigOptions(
	val cardinality: String?, //range
	val optional:Boolean?,
	val required: Boolean?,
	val type_key_filter: ReversibleList<String>?, //type | typeList,
	val severity: String?, //severity
	val push_scope: String?, //scope
	val replace_scope: Map<String,String>?, //scopeMap
	val scopes:List<String>?, //scope | scopeList
	val graph_related_types: List<String>? //graphTypeList
)