package icu.windea.pls.config

/**
 * @property inputScopes input_scopes | inputscopes: string[]
 * @property outputScope output_scope: string
 */
data class CwtLinkConfig(
	val inputScopes:List<String>,
	val outputScope:String
)

