package icu.windea.pls.model.scope

import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode

object ParadoxScopeContextResolver {
    fun getAny(): ParadoxScopeContext {
        return get(ParadoxScopeId.anyScopeId, ParadoxScopeId.anyScopeId)
    }

    fun getUnknown(input: ParadoxScopeContext? = null, isFrom: Boolean = false): ParadoxScopeContext {
        if (input == null) return get(ParadoxScopeId.unknownScopeId)
        return resolveNext(input, ParadoxScopeId.unknownScopeId, isFrom)
    }

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

    fun resolveNext(input: ParadoxScopeContext, pushScope: String?, isFrom: Boolean = false): ParadoxScopeContext {
        if (pushScope == null) return input // transfer current scope context
        val scope = ParadoxScope.get(pushScope)
        val root = if (isFrom) null else input.root
        val from = if (isFrom) null else input.from
        val fromFrom = if (isFrom) null else input.fromFrom
        val fromFromFrom = if (isFrom) null else input.fromFromFrom
        val fromFromFromFrom = if (isFrom) null else input.fromFromFromFrom
        val prevStack = input.prevStack.toMutableList().also { it.add(0, input) }
        return ParadoxScopeContext.Default(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
    }

    fun resolveNext(input: ParadoxScopeContext, next: ParadoxScopeContext, isFrom: Boolean = false): ParadoxScopeContext {
        val scope = next.scope
        val root = input.root
        val from = if (isFrom) next.from else next.from ?: input.from
        val fromFrom = if (isFrom) next.fromFrom else next.fromFrom ?: input.fromFrom
        val fromFromFrom = if (isFrom) next.fromFromFrom else next.fromFromFrom ?: input.fromFromFrom
        val fromFromFromFrom = if (isFrom) next.fromFromFromFrom else next.fromFromFromFrom ?: input.fromFromFromFrom
        val prevStack = input.prevStack.toMutableList().also { it.add(0, input) }
        return ParadoxScopeContext.Default(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
    }

    fun resolveNext(input: ParadoxScopeContext, links: List<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>>): ParadoxScopeContext {
        val prevStack = input.prevStack.toMutableList().also { it.add(0, input) }
        return ParadoxScopeContext.Linked(links, prevStack)
    }
}
