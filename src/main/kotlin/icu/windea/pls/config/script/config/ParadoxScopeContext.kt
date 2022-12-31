package icu.windea.pls.config.script.config

import icu.windea.pls.config.script.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.model.*

data class ParadoxScopeContext(
	val thisScope: String,
	val rootScope: String? = null,
	val fromScope: String? = null,
	val fromFromScope: String? = null,
	val fromFromFromScope: String? = null,
	val fromFromFromFromScope: String? = null
) {
	@Volatile var root: ParadoxScopeContext? = null // set when init
	@Volatile var prev: ParadoxScopeContext? = null // lazy set
	@Volatile var from: ParadoxScopeContext? = null // set when init
	
	//scope context before scope switch
	@Volatile var parent: ParadoxScopeContext? = null
	
	//scope context list of scope field expression nodes
	@Volatile var scopeFieldInfo: List<Tuple2<ParadoxScopeExpressionNode, ParadoxScopeContext>>? = null
	
	val supportAnyThisScope = thisScope == ParadoxScopeConfigHandler.anyScopeId
	
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
	
	init {
		if(rootScope != null) {
			if(rootScope == thisScope) {
				root = this
			} else {
				root = ParadoxScopeContext(rootScope, rootScope)
			}
		}
		if(fromScope != null) {
			val fromContext = ParadoxScopeContext(fromScope)
			if(fromFromScope != null) {
				val fromFromContext = ParadoxScopeContext(fromFromScope)
				if(fromFromFromScope != null) {
					val fromFromFromContext = ParadoxScopeContext(fromFromFromScope)
					if(fromFromFromFromScope != null) {
						val fromFromFromFromContext = ParadoxScopeContext(fromFromFromFromScope)
						fromFromFromContext.from = fromFromFromFromContext
					}
					fromFromContext.from = fromFromFromContext
				}
				fromContext.from = fromFromContext
			}
			from = fromContext
		}
	}
	
	fun resolve(pushScope: String?): ParadoxScopeContext {
		if(pushScope == null) return this
		val result = copy(thisScope = pushScope)
		result.prev = this
		result.parent = this
		return result
	}
	
	fun resolve(systemScopeContext: ParadoxScopeContext): ParadoxScopeContext{
		val result = systemScopeContext.copy()
		result.prev = systemScopeContext.prev
		result.parent = this
		return result
	}
	
	companion object {
		fun resolve(
			thisScope: String,
			rootScope: String? = null,
			fromScope: String? = null,
			fromFromScope: String? = null,
			fromFromFromScope: String? = null,
			fromFromFromFromScope: String? = null
		): ParadoxScopeContext {
			return ParadoxScopeContext(thisScope, rootScope, fromScope, fromFromScope, fromFromFromScope, fromFromFromFromScope)
		}
		
		fun resolve(map: Map<String, String?>): ParadoxScopeContext? {
			val thisScope = map.get("this") ?: return null
			val rootScope = map.get("root")
			val fromScope = map.get("from")
			val fromFromScope = map.get("fromfrom")
			val fromFromFromScope = map.get("fromfromfrom")
			val fromFromFromFromScope = map.get("fromfromfromfrom")
			return ParadoxScopeContext(thisScope, rootScope, fromScope, fromFromScope, fromFromFromScope, fromFromFromFromScope)
		}
	}
}
