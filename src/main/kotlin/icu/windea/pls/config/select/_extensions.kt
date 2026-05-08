package icu.windea.pls.config.select

inline fun <R> selectConfigScope(block: CwtConfigSelectScope.() -> R): R {
    val scope = CwtConfigSelectScopeImpl()
    return block.invoke(scope)
}
