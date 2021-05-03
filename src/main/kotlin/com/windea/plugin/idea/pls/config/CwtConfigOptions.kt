package com.windea.plugin.idea.pls.config

import com.windea.plugin.idea.pls.*

/**
 * @property cardinality range
 * @property optional value
 * @property required value
 * @property type_key_filter type | typeList
 * @property severity severity
 * @property push_scope scope
 * @property replace_scope scopeMap
 * @property scopes scope | scopeList
 * @property graph_related_types graphTypeList
 */
data class CwtConfigOptions(
	val cardinality: String?, //range
	val optional:Boolean?, 
	val required: Boolean?,
	val type_key_filter: ReversibleList<String>?, //type | typeList
	val severity: String?, //severity
	val push_scope: String?, //scope
	val replace_scope: Map<String,String>?, //scopeMap
	val scopes:List<String>?, //scope | scopeList
	val graph_related_types: List<String>? //graphTypeList
)