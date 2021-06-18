package icu.windea.pls.config

/**
 * @property internal_id int
 * @property supported_scopes string[]
 */
data class CwtModifyCategoryConfig(
	val internal_id: Int,
	val supported_scopes:List<String>
)