package icu.windea.pls.model.scope

import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.optimized
import icu.windea.pls.core.toCapitalizedWords
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeNode

object ParadoxScopeResolver {
    private val cache = CacheBuilder("maximumSize=1024, expireAfterAccess=30m").build<String, ParadoxScope> { ParadoxScope.Default(it) }

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
        val scope = map.get("this")?.let { resolveScope(it) } ?: return null
        val root = map.get("root")?.let { resolveScopeContext(it) }
        val from = map.get("from")?.let { resolveScopeContext(it) }
        val from2 = map.get("fromfrom")?.let { resolveScopeContext(it) }
        val from3 = map.get("fromfromfrom")?.let { resolveScopeContext(it) }
        val from4 = map.get("fromfromfromfrom")?.let { resolveScopeContext(it) }
        val prev = map.get("prev")?.let { resolveScopeContext(it) }
        val prev2 = map.get("prevprev")?.let { resolveScopeContext(it) }
        val prev3 = map.get("prevprevprev")?.let { resolveScopeContext(it) }
        val prev4 = map.get("prevprevprevprev")?.let { resolveScopeContext(it) }
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
        val fromFrom = if (isFrom) null else input.from2
        val fromFromFrom = if (isFrom) null else input.from3
        val fromFromFromFrom = if (isFrom) null else input.from4
        val prevStack = input.prevStack.toMutableList().also { it.add(0, input) }
        return ParadoxScopeContext.Complex(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
    }

    fun resolveNextScopeContext(input: ParadoxScopeContext, next: ParadoxScopeContext, isFrom: Boolean = false): ParadoxScopeContext {
        val scope = next.scope
        val root = input.root
        val from = if (isFrom) next.from else next.from ?: input.from
        val fromFrom = if (isFrom) next.from2 else next.from2 ?: input.from2
        val fromFromFrom = if (isFrom) next.from3 else next.from3 ?: input.from3
        val fromFromFromFrom = if (isFrom) next.from4 else next.from4 ?: input.from4
        val prevStack = input.prevStack.toMutableList().also { it.add(0, input) }
        return ParadoxScopeContext.Complex(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
    }

    fun resolveNextScopeContext(input: ParadoxScopeContext, links: List<Tuple2<ParadoxScopeNode, ParadoxScopeContext>>): ParadoxScopeContext {
        val prevStack = input.prevStack.toMutableList().also { it.add(0, input) }
        return ParadoxScopeContext.Linked(links, prevStack)
    }

    fun toScopeMap(scopeContext: ParadoxScopeContext, showFrom: Boolean = true, showPrev: Boolean = true): Map<String, ParadoxScope> {
        return buildMap {
            put("this", scopeContext.scope)
            scopeContext.root?.let { put("root", it.scope) }
            if (showFrom) {
                scopeContext.from?.let { put("from", it.scope) }
                scopeContext.from2?.let { put("fromfrom", it.scope) }
                scopeContext.from3?.let { put("fromfromfrom", it.scope) }
                scopeContext.from4?.let { put("fromfromfromfrom", it.scope) }
            }
            if (showPrev) {
                scopeContext.prev?.let { put("prev", it.scope) }
                scopeContext.prev2?.let { put("prevprev", it.scope) }
                scopeContext.prev3?.let { put("prevprevprev", it.scope) }
                scopeContext.prev4?.let { put("prevprevprevprev", it.scope) }
            }
        }
    }

    fun toScopeIdMap(scopeContext: ParadoxScopeContext, showFrom: Boolean = true, showPrev: Boolean = true): Map<String, String> {
        return buildMap {
            put("this", scopeContext.scope.id)
            scopeContext.root?.let { put("root", it.scope.id) }
            if (showFrom) {
                scopeContext.from?.let { put("from", it.scope.id) }
                scopeContext.from2?.let { put("fromfrom", it.scope.id) }
                scopeContext.from3?.let { put("fromfromfrom", it.scope.id) }
                scopeContext.from4?.let { put("fromfromfromfrom", it.scope.id) }
            }
            if (showPrev) {
                scopeContext.prev?.let { put("prev", it.scope.id) }
                scopeContext.prev2?.let { put("prevprev", it.scope.id) }
                scopeContext.prev3?.let { put("prevprevprev", it.scope.id) }
                scopeContext.prev4?.let { put("prevprevprevprev", it.scope.id) }
            }
        }
    }

    fun toPresentableString(scopeContext: ParadoxScopeContext, separator: String, showFrom: Boolean = true, showPrev: Boolean = true): String {
        val map = toScopeMap(scopeContext, showFrom, showPrev)
        return map.entries.joinToString(separator) { (k, v) -> "$k = $v" }
    }
}
