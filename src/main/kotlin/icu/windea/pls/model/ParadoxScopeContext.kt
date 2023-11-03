package icu.windea.pls.model

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.overridden.*

class ParadoxScopeContext private constructor(
    @Volatile var scope: ParadoxScope
) : UserDataHolderBase() {
    @Volatile var root: ParadoxScopeContext? = null
    @Volatile var prev: ParadoxScopeContext? = null
    @Volatile var from: ParadoxScopeContext? = null
    
    //scope context before scope switch
    @Volatile var parent: ParadoxScopeContext? = null
    
    val map by lazy {
        buildMap {
            put("this", scope)
            root?.let { put("root", it.scope) }
            from?.let { put("from", it.scope) }
            from?.from?.let { put("fromfrom", it.scope) }
            from?.from?.from?.let { put("fromfromfrom", it.scope) }
            from?.from?.from?.from?.let { put("fromfromfromfrom", it.scope) }
        }
    }
    
    val detailMap by lazy {
        buildMap {
            put("this", scope)
            root?.let { put("root", it.scope) }
            prev?.let { put("prev", it.scope) }
            prev?.prev?.let { put("prevprev", it.scope) }
            prev?.prev?.prev?.let { put("prevprevprev", it.scope) }
            prev?.prev?.prev?.prev?.let { put("prevprevprevprev", it.scope) }
            from?.let { put("from", it.scope) }
            from?.from?.let { put("fromfrom", it.scope) }
            from?.from?.from?.let { put("fromfromfrom", it.scope) }
            from?.from?.from?.from?.let { put("fromfromfromfrom", it.scope) }
        }
    }
    
    fun isEquivalentTo(other: ParadoxScopeContext?): Boolean {
        //note that root === this is possible
        if(this === other) return true
        if(other !is ParadoxScopeContext) return false
        if(scope != other.scope) return false
        if((root === this) xor (other.root === other)) return false
        if(root !== this && root != other.root) return false
        if((prev === this) xor (other.prev === other)) return false
        if(prev !== this && prev != other.prev) return false
        if((from === this) xor (other.from === other)) return false
        if(from !== this && from != other.from) return false
        return true
    }
    
    fun copy(): ParadoxScopeContext {
        val result = ParadoxScopeContext(scope)
        result.root = root
        result.prev = prev
        result.from = from
        result.parent = parent
        return result
    }
    
    fun copyAsInferred(): ParadoxScopeContext {
        val result = ParadoxScopeContext(ParadoxScope.inferred(scope.id))
        result.root = if(root === this) result else root?.copyAsInferred()
        result.prev = if(prev === this) result else prev?.copyAsInferred()
        result.from = if(from === this) result else from?.copyAsInferred()
        result.parent = parent
        return result
    }
    
    fun resolve(pushScope: String?): ParadoxScopeContext {
        //push_scope = null -> transfer scope
        if(pushScope == null) return this
        val result = ParadoxScopeContext(ParadoxScope.of(pushScope))
        result.root = this.root
        result.prev = this
        result.from = this.from
        result.parent = this
        return result
    }
    
    fun resolve(scopeContext: ParadoxScopeContext, isFrom: Boolean = false): ParadoxScopeContext {
        val result = scopeContext.copy()
        result.root = this.root
        result.prev = this
        if(!isFrom) {
            result.from = this.from
        }
        result.parent = this
        return result
    }
    
    companion object {
        val EMPTY = ParadoxScopeContext(ParadoxScope.AnyScope)
        
        fun resolve(thisScope: String): ParadoxScopeContext {
            return ParadoxScopeContext(ParadoxScope.of(thisScope))
        }
        
        fun resolve(thisScope: String, rootScope: String? = null): ParadoxScopeContext {
            val result = ParadoxScopeContext(ParadoxScope.of(thisScope))
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
            val result = ParadoxScopeContext(ParadoxScope.of(thisScope))
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
        
        fun resolve(scope: ParadoxScope, root: ParadoxScopeContext?, prev: ParadoxScopeContext?, from: ParadoxScopeContext?): ParadoxScopeContext {
            val result = ParadoxScopeContext(scope)
            result.root = root
            result.prev = prev
            result.from = from
            return result
        }
    }
    
    object Keys: KeyHolder
}

val ParadoxScopeContext.Keys.overriddenProvider by createKey<ParadoxOverriddenScopeContextProvider>("paradox.scopeContext.overriddenProvider")
val ParadoxScopeContext.Keys.scopeFieldInfo by createKey<List<Tuple2<ParadoxScopeFieldExpressionNode, ParadoxScopeContext>>>("paradox.scopeContext.scopeFieldInfo")

var ParadoxScopeContext.overriddenProvider by ParadoxScopeContext.Keys.overriddenProvider
var ParadoxScopeContext.scopeFieldInfo by ParadoxScopeContext.Keys.scopeFieldInfo //scope context list of scope field expression nodes