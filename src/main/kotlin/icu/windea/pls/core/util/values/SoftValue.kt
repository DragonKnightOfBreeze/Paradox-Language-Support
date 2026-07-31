package icu.windea.pls.core.util.values

import java.lang.ref.SoftReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * 在内部维护一个软引用，并在解引用时按需重新创建的值（包装类）。
 *
 * @see SoftReference
 */
abstract class SoftValue<T> {
    private var _value: SoftReference<T> = SoftReference(createValue())

    protected abstract fun createValue(): T

    fun dereference(): T {
        var value = _value.get()
        if (value != null) return value
        value = createValue()
        _value = SoftReference(value) // 此时 value 有局部强引用，不会被瞬时 GC
        return value
    }

    override fun toString(): String {
        return dereference().toString()
    }

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline operator fun <T> invoke(noinline valueProvider: () -> T): SoftValue<T> {
            return create(valueProvider)
        }

        @Suppress("unused")
        @JvmStatic
        fun <T> create(valueProvider: () -> T): SoftValue<T> {
            return object : SoftValue<T>() {
                override fun createValue(): T = valueProvider()
            }
        }

        @Suppress("unused")
        @JvmStatic
        fun <K, V> ofMutableMap(): SoftValue<MutableMap<K, V>> {
            return object : SoftValue<MutableMap<K, V>>() {
                override fun createValue(): MutableMap<K, V> = mutableMapOf()
            }
        }

        @Suppress("unused")
        @JvmStatic
        fun <K, V> ofConcurrentMap(): SoftValue<ConcurrentMap<K, V>> {
            return object : SoftValue<ConcurrentMap<K, V>>() {
                override fun createValue(): ConcurrentMap<K, V> = ConcurrentHashMap()
            }
        }
    }
}
