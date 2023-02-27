package icu.windea.pls.lang.model

import icu.windea.pls.core.*
import icu.windea.pls.core.expression.nodes.*

class ParadoxScopeContext private constructor(val scopeId: String) {
    @Volatile var root: ParadoxScopeContext? = null
    @Volatile var prev: ParadoxScopeContext? = null
    @Volatile var from: ParadoxScopeContext? = null
    @Volatile var parent: ParadoxScopeContext? = null //scope context before scope switch, not 'prev'
    
    //scope context list of scope field expression nodes
    @Volatile var scopeFieldInfo: List<Tuple2<ParadoxScopeExpressionNode, ParadoxScopeContext>>? = null
    
    val map by lazy {
        buildMap {
            put("this", scopeId)
            root?.let { put("root", it.scopeId) }
            from?.let { put("from", it.scopeId) }
            from?.from?.let { put("fromfrom", it.scopeId) }
            from?.from?.from?.let { put("fromfromfrom", it.scopeId) }
            from?.from?.from?.from?.let { put("fromfromfromfrom", it.scopeId) }
        }
    }
    
    val detailMap by lazy {
        buildMap {
            put("this", scopeId)
            root?.let { put("root", it.scopeId) }
            prev?.let { put("prev", it.scopeId) }
            prev?.prev?.let { put("prevprev", it.scopeId) }
            prev?.prev?.prev?.let { put("prevprevprev", it.scopeId) }
            prev?.prev?.prev?.prev?.let { put("prevprevprevprev", it.scopeId) }
            from?.let { put("from", it.scopeId) }
            from?.from?.let { put("fromfrom", it.scopeId) }
            from?.from?.from?.let { put("fromfromfrom", it.scopeId) }
            from?.from?.from?.from?.let { put("fromfromfromfrom", it.scopeId) }
        }
    }
    
    fun copy(): ParadoxScopeContext {
        val result = ParadoxScopeContext(scopeId)
        result.root = root
        result.prev = prev
        result.from = from
        result.parent = parent
        return result
    }
    
    fun resolve(pushScope: String?): ParadoxScopeContext {
        //push_scope = null > transfer scope
        if(pushScope == null) return this
        val result = ParadoxScopeContext(pushScope)
        result.prev = this
        result.root = this.root
        result.from = this.from
        result.parent = this
        return result
    }
    
    fun resolve(scopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val result = scopeContext.copy()
        result.parent = this
        return result
    }
    
    companion object {
        fun resolve(thisScope: String): ParadoxScopeContext {
            return ParadoxScopeContext(thisScope)
        }
        
        fun resolve(thisScope: String, rootScope: String? = null): ParadoxScopeContext {
            val result = ParadoxScopeContext(thisScope)
            rootScope?.let {
                val root = resolve(it)
                root.root = root
                result.root = root
            }
            return result
        }
        
        fun resolve(map: Map<String, String?>): ParadoxScopeContext? {
            val thisScope = map.get("this") ?: return null
            val rootScope = map.get("root")
            val fromScope = map.get("from")
            val fromFromScope = map.get("fromfrom")
            val fromFromFromScope = map.get("fromfromfrom")
            val fromFromFromFromScope = map.get("fromfromfromfrom")
            val result = ParadoxScopeContext(thisScope)
            rootScope?.let {
                val root = resolve(it)
                root.root = root
                result.root = root
            }
            fromScope?.let {
                val from = resolve(it)
                result.from = from
            }
            fromFromScope?.let {
                val fromFrom = resolve(it)
                result.from?.from = fromFrom
            }
            fromFromFromScope?.let {
                val fromFromFrom = resolve(it)
                result.from?.from?.from = fromFromFrom
            }
			fromFromFromFromScope?.let {
				val fromFromFromFrom = resolve(it)
				result.from?.from?.from?.from = fromFromFromFrom
			}
            return result
        }
    }
}
