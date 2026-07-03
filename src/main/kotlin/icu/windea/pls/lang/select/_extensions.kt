package icu.windea.pls.lang.select

inline fun <R> selectScope(
    scope: ParadoxPsiSelectScope = ParadoxPsiSelectScope.INSTANCE,
    block: ParadoxPsiSelectScope.() -> R
): R {
    return block.invoke(scope)
}
