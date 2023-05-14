package icu.windea.pls.core.collections

/**
 * 一个简单的LRUCache的实现。
 */
class LRUCache<K, V>(
    val maxSize: Int
): LinkedHashMap<K,V>() {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > maxSize
    }
}
