package icu.windea.pls.model.scope

import com.github.benmanes.caffeine.cache.Interner.*
import com.google.common.collect.ImmutableList
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.equalsFast
import icu.windea.pls.core.toCapitalizedWords
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Optimized
object ParadoxScopeResolver {
    private val interner = newWeakInterner<String>()
    private val indexCounter = AtomicInteger()
    // order-insensitive & cannot use weak value map atm (since scope objects are not directly stored in config groups)
    private val cache = ConcurrentHashMap<String, ParadoxScope>()
    // optimize memory & not thread safe & cannot use weak value map atm (since scope objects are not directly stored in config groups)
    private val indexMap = Int2ObjectOpenHashMap<ParadoxScope>()

    fun getScopeId(scope: String): String {
        val scopeId = scope.lowercase().replace(' ', '_')
        // "all" scope is always resolved as "any" scope
        if (scopeId.equalsFast(ParadoxScopeConstants.allScope)) return ParadoxScopeConstants.anyScope
        // intern to optimize memory
        return interner.intern(scopeId)
    }

    fun getScopeName(scope: String, configGroup: CwtConfigGroup): String {
        // handle "any" and "all" scope
        if (scope.equalsFast(ParadoxScopeConstants.anyScope, true)) return "Any"
        if (scope.equalsFast(ParadoxScopeConstants.allScope, true)) return "All"
        // a scope may not have aliases, or not defined in scopes.cwt
        return configGroup.scopes[scope]?.name
            ?: configGroup.scopeAliasMap[scope]?.name
            ?: scope.toCapitalizedWords()
    }

    fun getScopeByIndex(index: Int): ParadoxScope? {
        if (index == -1) return ParadoxScope.Any
        if (index == -2) return ParadoxScope.Unknown
        return synchronized(indexMap) { indexMap.get(index) }
    }

    fun resolveScope(id: String): ParadoxScope {
        if (id.equalsFast(ParadoxScopeConstants.anyScope)) return ParadoxScope.Any
        if (id.equalsFast(ParadoxScopeConstants.allScope)) return ParadoxScope.Any
        if (id.equalsFast(ParadoxScopeConstants.unknownScope)) return ParadoxScope.Unknown
        return cache.computeIfAbsent(id) {
            ParadoxScope.Default(id, indexCounter.getAndIncrement()).also {
                synchronized(indexMap) { indexMap.put(it.index, it) }
            }
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
        val prevStack = ImmutableList.builder<ParadoxScopeContext>().apply action@{
            prev?.let { add(it) } ?: return@action
            prev2?.let { add(it) } ?: return@action
            prev3?.let { add(it) } ?: return@action
            prev4?.let { add(it) } ?: return@action
        }.build()
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
        val prevStack = ImmutableList.builderWithExpectedSize<ParadoxScopeContext>(input.prevStack.size + 1)
            .add(input)
            .addAll(input.prevStack)
            .build()
        return ParadoxScopeContext.Complex(scope, root, from, from2, from3, from4, prevStack)
    }

    fun resolveNextScopeContext(input: ParadoxScopeContext, next: ParadoxScopeContext, isFrom: Boolean = false): ParadoxScopeContext {
        val scope = next.scope
        val root = input.root
        val from = if (isFrom) next.from else next.from ?: input.from
        val from2 = if (isFrom) next.from2 else next.from2 ?: input.from2
        val from3 = if (isFrom) next.from3 else next.from3 ?: input.from3
        val from4 = if (isFrom) next.from4 else next.from4 ?: input.from4
        val prevStack = ImmutableList.builderWithExpectedSize<ParadoxScopeContext>(input.prevStack.size + 1)
            .add(input)
            .addAll(input.prevStack)
            .build()
        return ParadoxScopeContext.Complex(scope, root, from, from2, from3, from4, prevStack)
    }

    fun resolveNextScopeContext(input: ParadoxScopeContext, links: List<Tuple2<ParadoxScopeNode, ParadoxScopeContext>>): ParadoxScopeContext {
        val prevStack = ImmutableList.builderWithExpectedSize<ParadoxScopeContext>(input.prevStack.size + 1)
            .add(input)
            .addAll(input.prevStack)
            .build()
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
