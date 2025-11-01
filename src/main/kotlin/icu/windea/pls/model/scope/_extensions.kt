package icu.windea.pls.model.scope

fun ParadoxScopeContext.toScopeMap(showFrom: Boolean = true, showPrev: Boolean = true): Map<String, ParadoxScope> {
    return buildMap {
        put("this", scope)
        root?.let { put("root", it.scope) }
        if (showFrom) {
            from?.let { put("from", it.scope) }
            fromFrom?.let { put("fromfrom", it.scope) }
            fromFromFrom?.let { put("fromfromfrom", it.scope) }
            fromFromFromFrom?.let { put("fromfromfromfrom", it.scope) }
        }
        if (showPrev) {
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
        if (showFrom) {
            from?.let { put("from", it.scope.id) }
            fromFrom?.let { put("fromfrom", it.scope.id) }
            fromFromFrom?.let { put("fromfromfrom", it.scope.id) }
            fromFromFromFrom?.let { put("fromfromfromfrom", it.scope.id) }
        }
        if (showPrev) {
            prev?.let { put("prev", it.scope.id) }
            prevPrev?.let { put("prevprev", it.scope.id) }
            prevPrevPrev?.let { put("prevprevprev", it.scope.id) }
            prevPrevPrevPrev?.let { put("prevprevprevprev", it.scope.id) }
        }
    }
}
