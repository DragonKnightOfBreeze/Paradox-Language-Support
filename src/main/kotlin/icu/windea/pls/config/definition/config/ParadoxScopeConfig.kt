package icu.windea.pls.config.definition.config

data class ParadoxScopeConfig(
	val thisScope: String,
	val rootScope: String?,
	val fromScope: String?
) {
	@Volatile var parent: ParadoxScopeConfig? = null
	
	fun resolve(pushScope: String?): ParadoxScopeConfig {
		return when{
			pushScope != null -> ParadoxScopeConfig(pushScope, rootScope, fromScope)
			else -> this
		}
	}
	
	fun resolveNew(pushScope: String?): ParadoxScopeConfig {
		return when{
			pushScope != null -> ParadoxScopeConfig(pushScope, rootScope, fromScope).also { it.parent = this }
			else -> ParadoxScopeConfig(thisScope, rootScope, fromScope).also { it.parent = this }
		}
	}
}
