package icu.windea.pls.model

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.scope.*
import icu.windea.pls.model.expression.complex.nodes.*

interface ParadoxScopeContext : UserDataHolder {
    val scope: ParadoxScope
    val root: ParadoxScopeContext?
    val from: ParadoxScopeContext?
    val fromFrom: ParadoxScopeContext?
    val fromFromFrom: ParadoxScopeContext?
    val fromFromFromFrom: ParadoxScopeContext?
    
    val prevStack: List<ParadoxScopeContext>
    val prev: ParadoxScopeContext? get() = prevStack.getOrNull(0)
    val prevPrev: ParadoxScopeContext? get() = prevStack.getOrNull(1)
    val prevPrevPrev: ParadoxScopeContext? get() = prevStack.getOrNull(2)
    val prevPrevPrevPrev: ParadoxScopeContext? get() = prevStack.getOrNull(3)
    
    companion object Resolver
    
    object Keys : KeyRegistry("ParadoxScopeContext")
}

var ParadoxScopeContext.isExact: Boolean? by createKeyDelegate(ParadoxScopeContext.Keys)

//scope context list of scope field expression nodes
var ParadoxScopeContext.scopeFieldInfo: List<Tuple2<ParadoxScopeFieldExpressionNode, ParadoxScopeContext>>? by createKeyDelegate(ParadoxScopeContext.Keys)

var ParadoxScopeContext.overriddenProvider: ParadoxOverriddenScopeContextProvider? by createKeyDelegate(ParadoxScopeContext.Keys)

fun ParadoxScopeContext.toScopeMap(showFrom: Boolean = true, showPrev: Boolean = true): Map<String, ParadoxScope> {
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

fun ParadoxScopeContext.toScopeIdMap(showFrom: Boolean = true, showPrev: Boolean = true): Map<String, String> {
    return buildMap {
        put("this", scope.id)
        root?.let { put("root", it.scope.id) }
        if(showFrom) {
            from?.let { put("from", it.scope.id) }
            fromFrom?.let { put("fromfrom", it.scope.id) }
            fromFromFrom?.let { put("fromfromfrom", it.scope.id) }
            fromFromFromFrom?.let { put("fromfromfromfrom", it.scope.id) }
        }
        if(showPrev) {
            prev?.let { put("prev", it.scope.id) }
            prevPrev?.let { put("prevprev", it.scope.id) }
            prevPrevPrev?.let { put("prevprevprev", it.scope.id) }
            prevPrevPrevPrev?.let { put("prevprevprevprev", it.scope.id) }
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
    val fromFrom = map.get("fromfrom")?.let { ParadoxScopeContext.resolve(it) }
    val fromFromFrom = map.get("fromfromfrom")?.let { ParadoxScopeContext.resolve(it) }
    val fromFromFromFrom = map.get("fromfromfromfrom")?.let { ParadoxScopeContext.resolve(it) }
    val prev = map.get("prev")?.let { ParadoxScopeContext.resolve(it) }
    val prevPrev = map.get("prevprev")?.let { ParadoxScopeContext.resolve(it) }
    val prevPrevPrev = map.get("prevprevprev")?.let { ParadoxScopeContext.resolve(it) }
    val prevPrevPrevPrev = map.get("prevprevprevprev")?.let { ParadoxScopeContext.resolve(it) }
    val prevStack = listOfNotNull(prev, prevPrev, prevPrevPrev, prevPrevPrevPrev)
    return DefaultParadoxScopeContext(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
}

fun ParadoxScopeContext.resolveNext(pushScope: String?, isFrom: Boolean = false): ParadoxScopeContext {
    if(pushScope == null) return this //transfer original scope context
    val scope = ParadoxScope.of(pushScope)
    val root = this.root
    val from = if(isFrom) null else this.from
    val fromFrom = if(isFrom) null else this.fromFrom
    val fromFromFrom = if(isFrom) null else this.fromFromFrom
    val fromFromFromFrom = if(isFrom) null else this.fromFromFromFrom
    val prevStack = this.prevStack.toMutableList().also { it.add(0, this) }
    return DefaultParadoxScopeContext(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
}

fun ParadoxScopeContext.resolveNext(scopeContext: ParadoxScopeContext, isFrom: Boolean = false): ParadoxScopeContext {
    val scope = scopeContext.scope
    val root = this.root
    val from = if(isFrom) scopeContext.from else scopeContext.from ?: this.from
    val fromFrom = if(isFrom) scopeContext.fromFrom else scopeContext.fromFrom ?: this.fromFrom
    val fromFromFrom = if(isFrom) scopeContext.fromFromFrom else scopeContext.fromFromFrom ?: this.fromFromFrom
    val fromFromFromFrom = if(isFrom) scopeContext.fromFromFromFrom else scopeContext.fromFromFromFrom ?: this.fromFromFromFrom
    val prevStack = this.prevStack.toMutableList().also { it.add(0, this) }
    return DefaultParadoxScopeContext(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
}

private class SimpleParadoxScopeContext(
    override val scope: ParadoxScope
) : UserDataHolderBase(), ParadoxScopeContext {
    override val root: ParadoxScopeContext? get() = null
    override val from: ParadoxScopeContext? get() = null
    override val fromFrom: ParadoxScopeContext? get() = null
    override val fromFromFrom: ParadoxScopeContext? get() = null
    override val fromFromFromFrom: ParadoxScopeContext? get() = null
    override val prevStack: List<ParadoxScopeContext> get() = emptyList()
    
    override fun toString(): String {
        return toScopeMap().toString()
    }
}

private class DefaultParadoxScopeContext(
    override val scope: ParadoxScope,
    override val root: ParadoxScopeContext? = null,
    override val from: ParadoxScopeContext? = null,
    override val fromFrom: ParadoxScopeContext? = null,
    override val fromFromFrom: ParadoxScopeContext? = null,
    override val fromFromFromFrom: ParadoxScopeContext? = null,
    override val prevStack: List<ParadoxScopeContext> = emptyList()
) : UserDataHolderBase(), ParadoxScopeContext {
    override fun toString(): String {
        return toScopeMap().toString()
    }
}

private val EmptyParadoxScopeContext = SimpleParadoxScopeContext(ParadoxScope.Any)