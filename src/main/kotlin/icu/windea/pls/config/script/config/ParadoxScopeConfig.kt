package icu.windea.pls.config.script.config

data class ParadoxScopeConfig(
	val thisScope: String,
	val rootScope: String? = null,
	val fromScope: String? = null,
	val fromFromScope: String? = null,
	val fromFromFromScope: String? = null,
	val fromFromFromFromScope: String? = null
) {
	@Volatile var root: ParadoxScopeConfig? = null
	@Volatile var prev: ParadoxScopeConfig? = null
	@Volatile var from: ParadoxScopeConfig? = null
	
	init {
		if(rootScope != null) {
			if(rootScope == thisScope) {
				root = this
			} else {
				root = ParadoxScopeConfig(rootScope, rootScope)
			}
		}
		if(fromScope != null) {
			val fromConfig = ParadoxScopeConfig(fromScope)
			if(fromFromScope != null) {
				val fromFromConfig = ParadoxScopeConfig(fromFromScope)
				if(fromFromFromScope != null) {
					val fromFromFromConfig = ParadoxScopeConfig(fromFromFromScope)
					if(fromFromFromFromScope != null) {
						val fromFromFromFromConfig = ParadoxScopeConfig(fromFromFromFromScope)
						fromFromFromConfig.from = fromFromFromFromConfig
					}
					fromFromConfig.from = fromFromFromConfig
				}
				fromConfig.from = fromFromConfig
			}
			from = fromConfig
		}
	}
	
	val map by lazy { 
		buildMap { 
			put("this", thisScope)
			if(rootScope != null) put("root", rootScope)
			if(fromScope != null) put("from", fromScope)
			if(fromFromScope != null) put("fromfrom", fromFromScope)
			if(fromFromFromScope != null) put("fromfromfrom", fromFromFromScope)
			if(fromFromFromFromScope != null) put("fromfromfromfrom", fromFromFromFromScope)
		}
	}
	
	fun resolve(pushScope: String?): ParadoxScopeConfig {
		if(pushScope == null) return this
		val scopeConfig = copy(thisScope = pushScope)
		scopeConfig.prev = this
		return scopeConfig
	}
	
	companion object{
		fun resolve(map: Map<String, String?>) :ParadoxScopeConfig? {
			val thisScope = map.get("this") ?: return null
			val rootScope = map.get("root") ?: thisScope
			val fromScope = map.get("from")
			val fromFromScope = map.get("fromfrom")
			val fromFromFromScope = map.get("fromfromfrom")
			val fromFromFromFromScope = map.get("fromfromfromfrom")
			return ParadoxScopeConfig(thisScope, rootScope, fromScope, fromFromScope, fromFromFromScope, fromFromFromFromScope)
		}
	}
}
