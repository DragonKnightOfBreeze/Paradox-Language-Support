package icu.windea.pls.lang.select

inline fun <R> selectScope(block: ParadoxPsiSelectScope.() -> R): R {
    val scope = ParadoxPsiSelectScopeImpl()
    return block.invoke(scope)
}
