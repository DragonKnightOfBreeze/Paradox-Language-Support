package icu.windea.pls.config.select

inline fun <R> selectConfigScope(
    scope: CwtConfigSelectScope = CwtConfigSelectScope.INSTANCE,
    block: CwtConfigSelectScope.() -> R
): R {
    return block.invoke(scope)
}
