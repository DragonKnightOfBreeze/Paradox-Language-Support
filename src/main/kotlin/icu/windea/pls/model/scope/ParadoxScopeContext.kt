package icu.windea.pls.model.scope

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.complex.nodes.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.scope.*

interface ParadoxScopeContext: UserDataHolder {
    val scope: ParadoxScope
    val root: ParadoxScopeContext?
    val fromStack: List<ParadoxScopeContext>
    val prevStack: List<ParadoxScopeContext>
    
    val from: ParadoxScopeContext? get() = fromStack.getOrNull(0)
    val fromFrom: ParadoxScopeContext? get() = fromStack.getOrNull(1)
    val fromFromFrom: ParadoxScopeContext? get() = fromStack.getOrNull(2)
    val fromFromFromFrom: ParadoxScopeContext? get() = fromStack.getOrNull(3)
    
    val prev: ParadoxScopeContext? get() = prevStack.getOrNull(0)
    val prevPrev: ParadoxScopeContext? get() = prevStack.getOrNull(1)
    val prevPrevPrev: ParadoxScopeContext? get() = prevStack.getOrNull(2)
    val prevPrevPrevPrev: ParadoxScopeContext? get() = prevStack.getOrNull(3)
    
    companion object Resolver
    
    object Keys : KeyRegistry("ParadoxScopeContext")
}

var ParadoxScopeContext.overriddenProvider: ParadoxOverriddenScopeContextProvider? by createKeyDelegate(ParadoxScopeContext.Keys)
//scope context list of scope field expression nodes
var ParadoxScopeContext.scopeFieldInfo: List<Tuple2<ParadoxScopeFieldExpressionNode, ParadoxScopeContext>>? by createKeyDelegate(ParadoxScopeContext.Keys)

fun ParadoxScopeContext.toMap(showFrom: Boolean = true, showPrev: Boolean = true): Map<String, ParadoxScope> {
    return buildMap {
        put("this", scope)
        root?.let { put("root", it.scope) }
        if(showFrom) {
            from?.let { put("from", it.scope) }
            fromFrom?.let { put("fromfrom", it.scope) }
            fromFromFrom?.let { put("fromfromfrom", it.scope) }
            fromFromFromFrom?.let { put("fromfromfromfrom", it.scope) }
        }
        if(showPrev) {
            prev?.let { put("prev", it.scope) }
            prevPrev?.let { put("prevprev", it.scope) }
            prevPrevPrev?.let { put("prevprevprev", it.scope) }
            prevPrevPrevPrev?.let { put("prevprevprevprev", it.scope) }
        }
    }
}

val ParadoxScopeContext.Resolver.Empty: ParadoxScopeContext get() = EmptyParadoxScopeContext

fun ParadoxScopeContext.Resolver.resolve(thisScope: String): ParadoxScopeContext {
    return SimpleParadoxScopeContext(ParadoxScope.of(thisScope))
}

fun ParadoxScopeContext.Resolver.resolve(thisScope: String, rootScope: String? = null): ParadoxScopeContext {
    val scope = ParadoxScope.of(thisScope)
    val root = rootScope?.let { ParadoxScopeContext.resolve(it) }
    return DefaultParadoxScopeContext(scope, root)
}

fun ParadoxScopeContext.Resolver.resolve(map: Map<String, String>): ParadoxScopeContext? {
    val scope = map.get("this")?.let { ParadoxScope.of(it) } ?: return null
    val root = map.get("root")?.let { ParadoxScopeContext.resolve(it) }
    val from = map.get("from")?.let { ParadoxScopeContext.resolve(it) }
    val fromFrom = map.get("from")?.let { ParadoxScopeContext.resolve(it) }
    val fromFromFrom = map.get("fromfrom")?.let { ParadoxScopeContext.resolve(it) }
    val fromFromFromFrom = map.get("fromfromfrom")?.let { ParadoxScopeContext.resolve(it) }
    val prev = map.get("prev")?.let { ParadoxScopeContext.resolve(it) }
    val prevPrev = map.get("prev")?.let { ParadoxScopeContext.resolve(it) }
    val prevPrevPrev = map.get("prevprev")?.let { ParadoxScopeContext.resolve(it) }
    val prevPrevPrevPrev = map.get("prevprevprev")?.let { ParadoxScopeContext.resolve(it) }
    val fromStack = listOfNotNull(from, fromFrom, fromFromFrom, fromFromFromFrom)
    val prevStack = listOfNotNull(prev, prevPrev, prevPrevPrev, prevPrevPrevPrev)
    return DefaultParadoxScopeContext(scope, root, fromStack, prevStack)
}

fun ParadoxScopeContext.resolveNext(pushScope: String?): ParadoxScopeContext {
    if(pushScope == null) return this //transfer original scope context
    val scope = ParadoxScope.of(pushScope)
    val root = this.root
    val fromStack = this.fromStack
    val prevStack = this.prevStack.toMutableList().also { it.add(0, this) }
    return DefaultParadoxScopeContext(scope, root, fromStack, prevStack)
}

fun ParadoxScopeContext.resolveNext(scopeContext: ParadoxScopeContext, isFrom: Boolean = false): ParadoxScopeContext {
    val scope = scopeContext.scope
    val root = this.root
    val fromStack = when {
        isFrom -> scopeContext.fromStack
        else -> scopeContext.fromStack.toMutableList().also { this.fromStack.forEachIndexed { i, c -> it.set(i, c) } }
    }
    val prevStack = this.prevStack.toMutableList().also { it.add(0, this) }
    return DefaultParadoxScopeContext(scope, root, fromStack, prevStack)
}

private class SimpleParadoxScopeContext(
    override val scope: ParadoxScope
) : UserDataHolderBase(), ParadoxScopeContext {
    override val root: ParadoxScopeContext? get() = null
    override val fromStack: List<ParadoxScopeContext> get() = emptyList()
    override val prevStack: List<ParadoxScopeContext> get() = emptyList()
}

private class DefaultParadoxScopeContext(
    override val scope: ParadoxScope,
    override val root: ParadoxScopeContext? = null,
    override val fromStack: List<ParadoxScopeContext> = emptyList(),
    override val prevStack: List<ParadoxScopeContext> = emptyList()
): UserDataHolderBase(), ParadoxScopeContext

private val EmptyParadoxScopeContext = SimpleParadoxScopeContext(ParadoxScope.Any)