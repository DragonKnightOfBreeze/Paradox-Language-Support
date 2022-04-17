@file:Suppress("unused")

package icu.windea.pls

import com.google.common.base.Function
import com.google.common.cache.*
import java.nio.file.*

private const val maxCacheSize = 1000L

fun <K, V> createCache(builder: (K) -> V): LoadingCache<K, V> {
	return CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(CacheLoader.from(Function { builder(it) }))
} 