package icu.windea.pls.config.util.data

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.util.KeyProviders.*
import icu.windea.pls.core.util.getUserDataOrDefault
import kotlin.reflect.KProperty

class CwtOptionDataAccessorProvider<T>(
    val cached: Boolean = false,
    val action: CwtMemberConfig<*>.() -> T
) {
    fun get(cacheKey: String? = null): CwtOptionDataAccessor<T> {
        return when {
            cached && cacheKey != null -> object : CwtOptionDataAccessor<T> {
                private val keyProvider = NamedWithFactory<T, CwtMemberConfig<*>>(CwtMemberConfig.Keys, cacheKey) { action(this) }
                private val key = keyProvider.getKey()
                override fun get(config: CwtMemberConfig<*>) = config.getUserDataOrDefault(key)
            }
            else -> CwtOptionDataAccessor { action(it) }
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> CwtOptionDataAccessorProvider<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): CwtOptionDataAccessor<T> = get(property.name)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> CwtOptionDataAccessor<T>.getValue(thisRef: Any?, property: KProperty<*>): CwtOptionDataAccessor<T> = this
