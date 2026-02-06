package icu.windea.pls.model.scope

import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode

object ParadoxScopeContextResolver {
    fun get(thisScope: String): ParadoxScopeContext {
        return ParadoxScopeContext.Simple(ParadoxScope.get(thisScope))
    }

    fun get(thisScope: String, rootScope: String?): ParadoxScopeContext {
        val scope = ParadoxScope.get(thisScope)
        val root = rootScope?.let { ParadoxScopeContext.get(it) }
        return ParadoxScopeContext.Default(scope, root)
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
        return ParadoxScopeContext.Default(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
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
        return ParadoxScopeContext.Default(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
    }

    fun resolveNext(current: ParadoxScopeContext, scopeContext: ParadoxScopeContext, isFrom: Boolean = false): ParadoxScopeContext {
        val scope = scopeContext.scope
        val root = current.root
        val from = if (isFrom) scopeContext.from else scopeContext.from ?: current.from
        val fromFrom = if (isFrom) scopeContext.fromFrom else scopeContext.fromFrom ?: current.fromFrom
        val fromFromFrom = if (isFrom) scopeContext.fromFromFrom else scopeContext.fromFromFrom ?: current.fromFromFrom
        val fromFromFromFrom = if (isFrom) scopeContext.fromFromFromFrom else scopeContext.fromFromFromFrom ?: current.fromFromFromFrom
        val prevStack = current.prevStack.toMutableList().also { it.add(0, current) }
        return ParadoxScopeContext.Default(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
    }

    fun resolveNext(current: ParadoxScopeContext, links: List<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>>): ParadoxScopeContext {
        val prevStack = current.prevStack.toMutableList().also { it.add(0, current) }
        return ParadoxScopeContext.Linked(links, prevStack)
    }
}
