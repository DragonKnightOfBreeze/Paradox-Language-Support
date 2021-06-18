package icu.windea.pls.config

import icu.windea.pls.*

/**
 * @property typeKeyFilter (option) type_key_filter: string | string[]
 * @property pushScope (option) push_scope: scope
 * @property startsWith (option) starts_with: string
 * @property displayName (option) display_name: string
 * @property abbreviation (option) abbreviation: string
 * @property onlyIfNot (option) only_if_not: string[]
 */
data class CwtSubtypeConfig(
	val name: String,
	val config: CwtConfigProperty,
	val typeKeyFilter: ReversibleList<String>? = null,
	val pushScope: String? = null,
	val startsWith: String? = null,
	val displayName: String? = null,
	val abbreviation: String? = null,
	val onlyIfNot: List<String>? = null
)

