package icu.windea.pls.core.util.metadata

import com.intellij.openapi.util.Key
import com.intellij.util.keyFMap.KeyFMap
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.util.KeyWithDefault
import icu.windea.pls.core.util.KeyWithProducer

interface MetadataMapOnKeyFMap: MutableMetadataMap {
    // NOTE 3.0.1 暂时不考虑更加激进的底层数据结构
    //  - 比如，可以在底层仅使用 `bitmasks: Long` 和 `elements: Array<Any>` 这两个字段
    //  - 当元素个数大于2且不大于64时，这种策略可以获得更好的性能和内存占用

    var map: KeyFMap

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

    override fun <T> set(key: Key<T>, value: T?) {
        // no strict thread safe check here
        // see: com.intellij.openapi.util.UserDataHolderBase.putUserData
        val newMap = if (value == null) map.minus(key) else map.plus(key, value)
        map = newMap
    }

    override fun clearMetadata() {
        map = KeyFMap.EMPTY_MAP
    }

    override fun copyMetadataTo(other: MetadataMapOnKeyFMap) {
        other.map = map
    }

    override fun mergeMetadataTo(other: MetadataMapOnKeyFMap) {
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
