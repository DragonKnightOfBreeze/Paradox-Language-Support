package icu.windea.pls.config

import icu.windea.pls.*

/**
 * @property type_key_filter (option) stringList
 * @property push_scope (option) scope
 * @property starts_with (option) string
 * @property display_name (option) string
 * @property abbreviation (option) string
 */
data class CwtSubtypeConfig(
	val name:String,
	val config: CwtConfigProperty,
	var type_key_filter: ReversibleList<String>? = null,
	var push_scope:String? = null,
	var starts_with: String? = null,
	var display_name:String? = null,
	var abbreviation: String? = null,
)

