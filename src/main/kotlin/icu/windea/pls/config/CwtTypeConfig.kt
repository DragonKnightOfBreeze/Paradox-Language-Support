package icu.windea.pls.config

import icu.windea.pls.*

/**
 * @property path (property) path: string/path
 * @property pathStrict (property) path_strict: boolean
 * @property pathFile (property) path_file: string/fileName
 * @property pathExtension (property) path_extension: string/fileExtension
 * @property nameField (property) name_field: string/propertyKey
 * @property nameFromFile (property) name_from_file: boolean
 * @property typePerFile (property) type_per_file: boolean
 * @property typeKeyFilter (property) type_key_filter: boolean
 * @property unique (property) unique: boolean
 * @property severity (property) severity: string:severity
 * @property skipRootKey (property*) skip_root_key: string | string[]
 * @property subtypes (property*) subtype[?]: subtypeInfo
 * @property localisation (property*) localisation: localisationInfo
 * @property typeKeyFilter (option) type_key_filter: string | string[]
 * @property startsWith (option) starts_with: string
 * @property graphRelatedTypes (option) graph_related_types: graphRelatedType[]
 */
data class CwtTypeConfig(
	val name: String,
	var path: String? = null,
	var pathStrict: Boolean = false,
	var pathFile: String? = null,
	var pathExtension: String? = null,
	var nameField: String? = null,
	var nameFromFile: Boolean = false,
	var typePerFile: Boolean = false,
	var unique: Boolean = false,
	var severity: String? = null,
	
	val skipRootKey: MutableList<List<String>> = mutableListOf(),
	val localisation: MutableMap<String, MutableList<CwtTypeLocalisationConfig>> = mutableMapOf(),
	val subtypes: MutableMap<String, CwtSubtypeConfig> = mutableMapOf(),
	
	var typeKeyFilter: ReversibleList<String>? = null,
	var startsWith: String? = null,
	var graphRelatedTypes: List<String>? = null
)

