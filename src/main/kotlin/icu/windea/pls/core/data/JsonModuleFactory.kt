package icu.windea.pls.core.data

object JsonModuleFactory {
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T : Any> get(module: JsonModuleWithType<T>): JsonModuleWithType<T> = module
}
