package icu.windea.pls.lang.select

inline fun <R> selectScope(
    scope: ParadoxSelectScope = ParadoxSelectScope.INSTANCE,
    block: ParadoxSelectScope.() -> R
): R {
    return block.invoke(scope)
}
