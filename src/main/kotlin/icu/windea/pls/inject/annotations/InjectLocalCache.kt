package icu.windea.pls.inject.annotations

import icu.windea.pls.inject.support.*

/**
 * 注明本地缓存信息。
 *
 * 目标方法的实现会被转换成对本地缓存的访问。
 *
 * 这些方法必须有且仅有一个参数（作为缓存的键），且拥有返回值（作为缓存的值）。
 *
 * @property value 目标方法的名字。
 * @see LocalCacheCodeInjectorSupport
 * @see com.google.common.cache.LocalCache
 * @see com.google.common.cache.CacheBuilderSpec
 */
@Target(AnnotationTarget.CLASS)
@Repeatable
annotation class InjectLocalCache(
    val value: String,
    val spec: String = ""
)
