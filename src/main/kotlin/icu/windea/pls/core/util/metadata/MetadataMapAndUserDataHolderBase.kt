package icu.windea.pls.core.util.metadata

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.util.keyFMap.KeyFMap
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.util.KeyWithDefault
import icu.windea.pls.core.util.KeyWithProducer

/**
 * 使用 [KeyFMap] 作为底层数据结构，同时继承自 [UserDataHolderBase] 的元数据映射。
 *
 * @see MetadataMapBase
 */
open class MetadataMapAndUserDataHolderBase : UserDataHolderBase(), MetadataMap {
    // NOTE 3.0.1 暂时不考虑更加激进的底层数据结构
    //  - 比如，可以在底层仅使用 `bitmasks: Long` 和 `elements: Array<Any>` 这两个字段
    //  - 当元素个数大于2且不大于64时，这种策略可以获得更好的性能和内存占用

    // volatile is enough here since strict thread safe is not required
    @Volatile private var map: KeyFMap = KeyFMap.EMPTY_MAP

    override fun isEmpty(): Boolean {
        return map.isEmpty
    }

    override fun <T> get(key: Key<T>): T? {
        val value = map.get(key)
        @Suppress("UNCHECKED_CAST")
        if (value === EMPTY_OBJECT) return null as T
        return value
    }

    override fun <T> get(key: KeyWithDefault<T>): T {
        val value = map.get(key)
        @Suppress("UNCHECKED_CAST")
        if (value === EMPTY_OBJECT) return null as T
        return value ?: key.default
    }

    override fun <T> get(key: KeyWithProducer<T>): T {
        val value = map.get(key)
        @Suppress("UNCHECKED_CAST")
        if (value === EMPTY_OBJECT) return null as T
        if (value != null) return value
        // no strict thread safe check here
        // see: com.intellij.openapi.util.UserDataHolderBase.putUserData
        val computed = key.producer()
        @Suppress("UNCHECKED_CAST")
        val newMap = map.plus(key as Key<Any>, computed ?: EMPTY_OBJECT)
        map = newMap
        return computed
    }

    operator fun <T> set(key: Key<T>, value: T?) {
        // no strict thread safe check here
        // see: com.intellij.openapi.util.UserDataHolderBase.putUserData
        val newMap = if (value == null) map.minus(key) else map.plus(key, value)
        map = newMap
    }

    /** 清空所有元数据。 */
    @Suppress("unused")
    fun clearMetadata() {
        map = KeyFMap.EMPTY_MAP
    }

    /** 将当前元数据映射中的所有元数据拷贝到 [other]。[other] 中的底层数据结构会被直接替换。 */
    @Suppress("unused")
    fun copyMetadataTo(other: MetadataMapAndUserDataHolderBase) {
        other.map = map
    }

    /** 将当前元数据映射中的所有元数据合并到 [other]。[other] 中的底层元数据会被按需替换，已存在时也会被覆盖。 */
    @Suppress("unused")
    fun mergeMetadataTo(other: MetadataMapAndUserDataHolderBase) {
        if (map.isEmpty) return // fast return
        if (other.map.isEmpty) {
            other.map = map
        } else {
            var newOtherMap = other.map
            for (key in map.keys) {
                val value = map.get(key) ?: continue
                @Suppress("UNCHECKED_CAST")
                newOtherMap = newOtherMap.plus(key as Key<Any>, value)
            }
            other.map = newOtherMap
        }
    }
}
