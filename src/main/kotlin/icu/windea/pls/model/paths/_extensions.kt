package icu.windea.pls.model.paths

import icu.windea.pls.core.*

fun ParadoxPath.matches(matcher: ParadoxPathMatcher): Boolean {
    return matcher.matches(this)
}

fun ParadoxExpressionPath.relativeTo(other: ParadoxExpressionPath): ParadoxExpressionPath? {
    if (this == other) return ParadoxExpressionPath.resolveEmpty()
    if (this.isEmpty()) return other
    val path = other.path.removePrefixOrNull(this.path + "/") ?: return null
    return ParadoxExpressionPath.resolve(path)
}
