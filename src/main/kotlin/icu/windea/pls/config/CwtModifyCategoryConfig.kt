package icu.windea.pls.config

/**
 * @property internalId internal_id: int
 * @property supportedScopes supported_scopes: string[]
 */
data class CwtModifyCategoryConfig(
	val internalId: Int,
	val supportedScopes:List<String>
)