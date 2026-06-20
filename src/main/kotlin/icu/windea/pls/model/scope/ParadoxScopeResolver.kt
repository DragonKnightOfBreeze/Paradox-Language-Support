package icu.windea.pls.model.scope

import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.optimized
import icu.windea.pls.core.toCapitalizedWords
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.lang.resolve.complexExpression.nodes.*

object ParadoxScopeResolver {
    private val cache = CacheBuilder("expireAfterAccess=30m").build<String, ParadoxScope> { ParadoxScope.Default(it) }

    fun getScopeId(scope: String): String {
        val scopeId = scope.lowercase().replace(' ', '_').optimized() // optimized to optimize memory
        // "all" scope is always resolved as "any" scope
        if (scopeId == ParadoxScopeConstants.allScope) return ParadoxScopeConstants.anyScope
        return scopeId
    }

    fun getScopeName(scope: String, configGroup: CwtConfigGroup): String {
        // handle "any" and "all" scope
        if (scope.equals(ParadoxScopeConstants.anyScope, true)) return "Any"
        if (scope.equals(ParadoxScopeConstants.allScope, true)) return "All"
        // a scope may not have aliases, or not defined in scopes.cwt
        return configGroup.scopes[scope]?.name
            ?: configGroup.scopeAliasMap[scope]?.name
            ?: scope.toCapitalizedWords().optimized() // optimized to optimize memory
    }

    fun resolveScope(id: String): ParadoxScope {
        return when {
            id == ParadoxScope.Any.id -> ParadoxScope.Any
            id == ParadoxScope.Unknown.id -> ParadoxScope.Unknown
            else -> cache.get(id)
        }
    }

    fun resolveAnyScopeContext(): ParadoxScopeContext {
        return resolveScopeContext(ParadoxScopeConstants.anyScope, ParadoxScopeConstants.anyScope)
    }

    fun resolveUnknownScopeContext(input: ParadoxScopeContext? = null, isFrom: Boolean = false): ParadoxScopeContext {
        if (input == null) return resolveScopeContext(ParadoxScopeConstants.unknownScope)
        return resolveNextScopeContext(input, ParadoxScopeConstants.unknownScope, isFrom)
    }

    fun resolveScopeContext(thisScope: String): ParadoxScopeContext {
        val scope = resolveScope(thisScope)
        return ParadoxScopeContext.Simple(scope)
    }

    fun resolveScopeContext(thisScope: String, rootScope: String?): ParadoxScopeContext {
        val scope = resolveScope(thisScope)
        val root = rootScope?.let { resolveScopeContext(it) }
        return ParadoxScopeContext.Simple(scope, root)
    }

    fun resolveScopeContext(map: Map<String, String>): ParadoxScopeContext? {
        val c = ParadoxScopeConstants
        val scope = map.get(c.thisScope)?.let { resolveScope(it) } ?: return null
        val root = map.get(c.rootScope)?.let { resolveScopeContext(it) }
        val from = map.get(c.fromScope)?.let { resolveScopeContext(it) }
        val from2 = map.get(c.from2Scope)?.let { resolveScopeContext(it) }
        val from3 = map.get(c.from3Scope)?.let { resolveScopeContext(it) }
        val from4 = map.get(c.from4Scope)?.let { resolveScopeContext(it) }
        val prev = map.get(c.prevScope)?.let { resolveScopeContext(it) }
        val prev2 = map.get(c.prev2Scope)?.let { resolveScopeContext(it) }
        val prev3 = map.get(c.prev3Scope)?.let { resolveScopeContext(it) }
        val prev4 = map.get(c.prev4Scope)?.let { resolveScopeContext(it) }
        val prevStack = buildList b@{
            // break if previous-prev is null (but next-prev is null or not null)
            prev?.let { add(it) } ?: return@b
            prev2?.let { add(it) } ?: return@b
            prev3?.let { add(it) } ?: return@b
            prev4?.let { add(it) } ?: return@b
        }
        return ParadoxScopeContext.Complex(scope, root, from, from2, from3, from4, prevStack)
    }

    fun resolveNextScopeContext(input: ParadoxScopeContext, pushScope: String?, isFrom: Boolean = false): ParadoxScopeContext {
        if (pushScope == null) return input // transfer current scope context
        val scope = resolveScope(pushScope)
        val root = if (isFrom) null else input.root
        val from = if (isFrom) null else input.from
        val from2 = if (isFrom) null else input.from2
        val from3 = if (isFrom) null else input.from3
        val from4 = if (isFrom) null else input.from4
        val prevStack = input.prevStack.toMutableList().also { it.add(0, input) }
        return ParadoxScopeContext.Complex(scope, root, from, from2, from3, from4, prevStack)
    }

    fun resolveNextScopeContext(input: ParadoxScopeContext, next: ParadoxScopeContext, isFrom: Boolean = false): ParadoxScopeContext {
        val scope = next.scope
        val root = input.root
        val from = if (isFrom) next.from else next.from ?: input.from
        val from2 = if (isFrom) next.from2 else next.from2 ?: input.from2
        val from3 = if (isFrom) next.from3 else next.from3 ?: input.from3
        val from4 = if (isFrom) next.from4 else next.from4 ?: input.from4
        val prevStack = input.prevStack.toMutableList().also { it.add(0, input) }
        return ParadoxScopeContext.Complex(scope, root, from, from2, from3, from4, prevStack)
    }

    fun resolveNextScopeContext(input: ParadoxScopeContext, links: List<Tuple2<ParadoxScopeNode, ParadoxScopeContext>>): ParadoxScopeContext {
        val prevStack = input.prevStack.toMutableList().also { it.add(0, input) }
        return ParadoxScopeContext.Linked(links, prevStack)
    }

    fun toScopeMap(scopeContext: ParadoxScopeContext, showFrom: Boolean = true, showPrev: Boolean = true): Map<String, ParadoxScope> {
        val c = ParadoxScopeConstants
        val result = mutableMapOf<String, ParadoxScope>()
        fun putScope(key: String, scope: ParadoxScope?) = scope?.let { result[key] = it }
        result[c.thisScope] = scopeContext.scope
        putScope(c.rootScope, scopeContext.rootScope)
        if (showFrom) {
            putScope(c.fromScope, scopeContext.fromScope)
            putScope(c.from2Scope, scopeContext.from2Scope)
            putScope(c.from3Scope, scopeContext.from3Scope)
            putScope(c.from4Scope, scopeContext.from4Scope)
        }
        if (showPrev) {
            putScope(c.prevScope, scopeContext.prevScope)
            putScope(c.prev2Scope, scopeContext.prev2Scope)
            putScope(c.prev3Scope, scopeContext.prev3Scope)
            putScope(c.prev4Scope, scopeContext.prev4Scope)
        }
        return result
    }

    fun toScopeIdMap(scopeContext: ParadoxScopeContext, showFrom: Boolean = true, showPrev: Boolean = true): Map<String, String> {
        val c = ParadoxScopeConstants
        val result = mutableMapOf<String, String>()
        fun putScopeId(key: String, scope: ParadoxScope?) = scope?.let { result[key] = it.id }
        result[c.thisScope] = scopeContext.scope.id
        putScopeId(c.rootScope, scopeContext.rootScope)
        if (showFrom) {
            putScopeId(c.fromScope, scopeContext.fromScope)
            putScopeId(c.from2Scope, scopeContext.from2Scope)
            putScopeId(c.from3Scope, scopeContext.from3Scope)
            putScopeId(c.from4Scope, scopeContext.from4Scope)
        }
        if (showPrev) {
            putScopeId(c.prevScope, scopeContext.prevScope)
            putScopeId(c.prev2Scope, scopeContext.prev2Scope)
            putScopeId(c.prev3Scope, scopeContext.prev3Scope)
            putScopeId(c.prev4Scope, scopeContext.prev4Scope)
        }
        return result
    }

    fun toPresentableString(scopeContext: ParadoxScopeContext, separator: String, showFrom: Boolean = true, showPrev: Boolean = true): String {
        val map = toScopeMap(scopeContext, showFrom, showPrev)
        return map.entries.joinToString(separator) { (k, v) -> "$k = $v" }
    }
}
