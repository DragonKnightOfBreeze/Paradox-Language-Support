package icu.windea.pls.model.scope

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode

object ParadoxScopeContextResolver {
    fun get(thisScope: String): ParadoxScopeContext {
        return SimpleParadoxScopeContext(ParadoxScope.get(thisScope))
    }

    fun get(thisScope: String, rootScope: String?): ParadoxScopeContext {
        val scope = ParadoxScope.get(thisScope)
        val root = rootScope?.let { ParadoxScopeContext.get(it) }
        return DefaultParadoxScopeContext(scope, root)
    }

    fun get(map: Map<String, String>): ParadoxScopeContext? {
        val scope = map.get("this")?.let { ParadoxScope.get(it) } ?: return null
        val root = map.get("root")?.let { ParadoxScopeContext.get(it) }
        val from = map.get("from")?.let { ParadoxScopeContext.get(it) }
        val fromFrom = map.get("fromfrom")?.let { ParadoxScopeContext.get(it) }
        val fromFromFrom = map.get("fromfromfrom")?.let { ParadoxScopeContext.get(it) }
        val fromFromFromFrom = map.get("fromfromfromfrom")?.let { ParadoxScopeContext.get(it) }
        val prev = map.get("prev")?.let { ParadoxScopeContext.get(it) }
        val prevPrev = map.get("prevprev")?.let { ParadoxScopeContext.get(it) }
        val prevPrevPrev = map.get("prevprevprev")?.let { ParadoxScopeContext.get(it) }
        val prevPrevPrevPrev = map.get("prevprevprevprev")?.let { ParadoxScopeContext.get(it) }
        val prevStack = buildList b@{
            // break if previous-prev is null (but next-prev is null or not null)
            prev?.let { add(it) } ?: return@b
            prevPrev?.let { add(it) } ?: return@b
            prevPrevPrev?.let { add(it) } ?: return@b
            prevPrevPrevPrev?.let { add(it) } ?: return@b
        }
        return DefaultParadoxScopeContext(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
    }

    fun resolveNext(current: ParadoxScopeContext, pushScope: String?, isFrom: Boolean = false): ParadoxScopeContext {
        if (pushScope == null) return current // transfer current scope context
        val scope = ParadoxScope.get(pushScope)
        val root = if (isFrom) null else current.root
        val from = if (isFrom) null else current.from
        val fromFrom = if (isFrom) null else current.fromFrom
        val fromFromFrom = if (isFrom) null else current.fromFromFrom
        val fromFromFromFrom = if (isFrom) null else current.fromFromFromFrom
        val prevStack = current.prevStack.toMutableList().also { it.add(0, current) }
        return DefaultParadoxScopeContext(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
    }

    fun resolveNext(current: ParadoxScopeContext, scopeContext: ParadoxScopeContext, isFrom: Boolean = false): ParadoxScopeContext {
        val scope = scopeContext.scope
        val root = current.root
        val from = if (isFrom) scopeContext.from else scopeContext.from ?: current.from
        val fromFrom = if (isFrom) scopeContext.fromFrom else scopeContext.fromFrom ?: current.fromFrom
        val fromFromFrom = if (isFrom) scopeContext.fromFromFrom else scopeContext.fromFromFrom ?: current.fromFromFrom
        val fromFromFromFrom = if (isFrom) scopeContext.fromFromFromFrom else scopeContext.fromFromFromFrom ?: current.fromFromFromFrom
        val prevStack = current.prevStack.toMutableList().also { it.add(0, current) }
        return DefaultParadoxScopeContext(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
    }

    fun resolveNext(current: ParadoxScopeContext, links: List<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>>): ParadoxScopeContext {
        val prevStack = current.prevStack.toMutableList().also { it.add(0, current) }
        return LinkedParadoxScopeContext(links, prevStack)
    }
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
    override val links: List<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>> get() = emptyList()

    override fun toString(): String {
        return "SimpleParadoxScopeContext: " + toScopeIdMap().toString()
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
    override val links: List<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>> get() = emptyList()

    override fun toString(): String {
        return "DefaultParadoxScopeContext: " + toScopeIdMap().toString()
    }
}

private class LinkedParadoxScopeContext(
    override val links: List<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>>,
    override val prevStack: List<ParadoxScopeContext> = emptyList()
) : UserDataHolderBase(), ParadoxScopeContext {
    private val last = links.lastOrNull()?.second ?: throw IllegalArgumentException()

    override val scope: ParadoxScope get() = last.scope
    override val root: ParadoxScopeContext? get() = last.root
    override val from: ParadoxScopeContext? get() = last.from
    override val fromFrom: ParadoxScopeContext? get() = last.fromFrom
    override val fromFromFrom: ParadoxScopeContext? get() = last.fromFromFrom
    override val fromFromFromFrom: ParadoxScopeContext? get() = last.fromFromFromFrom

    override fun toString(): String {
        return "LinkedParadoxScopeContext: " + toScopeIdMap().toString()
    }
}
