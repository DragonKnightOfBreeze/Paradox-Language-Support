@file:Suppress("unused")

package icu.windea.pls.core.collections

import java.lang.ref.SoftReference
import java.util.concurrent.ConcurrentHashMap

/**
 * 被软引用的可变映射。
 *
 * 说明：
 * - 内部仅存储委托映射的软引用 [delegate]，各种 [MutableMap] 的操作方法将会委托给解引用后的委托映射。
 * - 使用 [clear] 清空映射时，将会直接丢弃旧映射并新建空映射。
 */
abstract class SoftMutableMap<K, V> : MutableMap<K, V> {
    @Volatile
    private var delegate: SoftReference<MutableMap<K, V>> = SoftReference(createDelegate())

    protected abstract fun createDelegate(): MutableMap<K, V>

    private fun dereference(): MutableMap<K, V> {
        var map = delegate.get()
        if (map != null) return map
        map = createDelegate()
        delegate = SoftReference(map) // 此时 map 有局部强引用，不会被瞬时 GC
        return map
    }

    override val keys: MutableSet<K> get() = dereference().keys
    override val values: MutableCollection<V> get() = dereference().values
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = dereference().entries
    override val size: Int get() = dereference().size

    override fun isEmpty(): Boolean = dereference().isEmpty()
    override fun containsKey(key: K): Boolean = dereference().containsKey(key)

    override fun containsValue(value: V): Boolean = dereference().containsValue(value)
    override fun get(key: K): V? = dereference().get(key)
    override fun put(key: K, value: V): V? = dereference().put(key, value)
    override fun putAll(from: Map<out K, V>) = dereference().putAll(from)
    override fun remove(key: K): V? = dereference().remove(key)

    override fun clear() {
        dereference().clear()
        delegate = SoftReference(createDelegate())
    }
}

class SoftConcurrentHashMap<K : Any, V : Any> : SoftMutableMap<K, V>() {
    override fun createDelegate(): MutableMap<K, V> {
        return ConcurrentHashMap()
    }
}
