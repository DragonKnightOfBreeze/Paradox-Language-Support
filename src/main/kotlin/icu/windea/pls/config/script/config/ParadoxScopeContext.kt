package icu.windea.pls.config.script.config

data class ParadoxScopeContext(
	val thisScope: String,
	val rootScope: String? = null,
	val fromScope: String? = null,
	val fromFromScope: String? = null,
	val fromFromFromScope: String? = null,
	val fromFromFromFromScope: String? = null
) {
	@Volatile var root: ParadoxScopeContext? = null
	@Volatile var prev: ParadoxScopeContext? = null
	@Volatile var from: ParadoxScopeContext? = null
	
	init {
		if(rootScope != null) {
			if(rootScope == thisScope) {
				root = this
			} else {
				root = ParadoxScopeContext(rootScope, rootScope)
			}
		}
		if(fromScope != null) {
			val fromConfig = ParadoxScopeContext(fromScope)
			if(fromFromScope != null) {
				val fromFromConfig = ParadoxScopeContext(fromFromScope)
				if(fromFromFromScope != null) {
					val fromFromFromConfig = ParadoxScopeContext(fromFromFromScope)
					if(fromFromFromFromScope != null) {
						val fromFromFromFromConfig = ParadoxScopeContext(fromFromFromFromScope)
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
	
	fun resolve(pushScope: String?): ParadoxScopeContext {
		if(pushScope == null) return this
		val scopeConfig = copy(thisScope = pushScope)
		scopeConfig.prev = this
		return scopeConfig
	}
	
	companion object{
		fun resolve(map: Map<String, String?>) :ParadoxScopeContext? {
			val thisScope = map.get("this") ?: return null
			val rootScope = map.get("root") ?: thisScope
			val fromScope = map.get("from")
			val fromFromScope = map.get("fromfrom")
			val fromFromFromScope = map.get("fromfromfrom")
			val fromFromFromFromScope = map.get("fromfromfromfrom")
			return ParadoxScopeContext(thisScope, rootScope, fromScope, fromFromScope, fromFromFromScope, fromFromFromFromScope)
		}
	}
}
