package icu.windea.pls.config.util.data

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.util.KeyProviders.NamedWithFactory
import icu.windea.pls.core.util.getUserDataOrDefault
import kotlin.reflect.KProperty

/**
 * 用于创建并获取选项属性访问器。
 *
 * @property cached 是否缓存到用户数据中。
 * @property action 选项数据的获取逻辑。
 *
 * @see CwtOptionDataAccessor
 */
class CwtOptionDataAccessorProvider<T>(
    private val cached: Boolean = false,
    private val action: CwtMemberConfig<*>.() -> T
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

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun getValue(thisRef: Any?, property: KProperty<*>): CwtOptionDataAccessor<T> {
        return get(property.name)
    }
}
