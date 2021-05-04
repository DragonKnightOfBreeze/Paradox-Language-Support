package com.windea.plugin.idea.pls.config

import com.windea.plugin.idea.pls.*

/**
 * @property path (property) path
 * @property path_strict (property) boolean
 * @property path_file (property) fileName
 * @property path_extension (property) fileExtension
 * @property name_field (property) propertyKey
 * @property name_from_file (property) boolean
 * @property type_key_filter (property) boolean
 * @property unique (property) boolean
 * @property severity (property) severity
 * @property skip_root_key (property) stringList*
 * @property subtypes (property) subtypeInfo*
 * @property localisation (property) localisationInfo*
 * @property type_key_filter (option) stringList
 * @property starts_with (option) string
 * @property graph_related_types (option) graphRelatedTypeList
 */
data class CwtTypeConfig(
	val name:String,
	var path:String? = null,
	var path_strict:Boolean = false,
	var path_file:String? = null,
	var path_extension:String? = null,
	var name_field:String? = null,
	var name_from_file:Boolean = false,
	var type_per_file:Boolean = false,
	var unique:Boolean = false,
	var severity:String? = null,
	
	val skip_root_key: MutableList<List<String>> = mutableListOf(),
	val localisation:MutableMap<String,MutableList<CwtTypeLocalisationConfig>> = mutableMapOf(),
	val subtypes: MutableMap<String,CwtSubtypeConfig> = mutableMapOf(),
	
	var type_key_filter: ReversibleList<String>? = null,
	var starts_with:String? = null,
	var graph_related_types:List<String>? = null
)

