@file:Suppress("unused")

package icu.windea.pls.core.util.values

import icu.windea.pls.core.annotations.Fast
import icu.windea.pls.core.toBoolean
import icu.windea.pls.core.toByte
import java.util.function.Supplier

/**
 * 可以延迟初始化的值（包装类）。
 *
 * 说明：
 * - 使用双重检查锁定（double-checked locking）保证初始化时的线程安全。
 * - 不同于 [Lazy]，不需要在声明时就指定初始化逻辑。
 */
class LazyValue<T> : Supplier<T?> {
    @Volatile private var _value: Any? = UNINITIALIZED

    @Suppress("UNCHECKED_CAST")
    var value: T?
        get() = if (isInitialized()) _value as? T else null
        set(value) {
            _value = value
        }

    override fun get(): T? = value

    fun isInitialized(): Boolean {
        return _value !== UNINITIALIZED
    }

    fun clear() {
        _value = UNINITIALIZED
    }

    inline fun check(crossinline predicate: (T) -> Boolean) {
        val value = value ?: return
        if (predicate(value)) return
        clear()
    }

    inline fun initialize(crossinline initializer: () -> T?): T? {
        if (isInitialized()) return value
        synchronized(this) {
            if (isInitialized()) return value
            val newValue = initializer()
            value = newValue
            return newValue
        }
    }

    inline fun reinitialize(crossinline initializer: () -> T?): T? {
        synchronized(this) {
            val newValue = initializer()
            value = newValue
            return newValue
        }
    }

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value is not initialized."

    companion object {
        const val UNINITIALIZED_BOOLEAN: Byte = -1
        @Suppress("RedundantNullableReturnType") val UNINITIALIZED: Any? = Any()

        /**
         * 用于实现内存友好的布尔类型的懒加载属性。基于 `@Volatile`。
         *
         * 示例：
         *
         * ```
         * val value: Boolean
         *     get() = LazyValue.ofBoolean({ _value }, { _value = it }) { computeValue() }
         * @Volatile private var _value = LazyValue.UNINITIALIZED_BOOLEAN
         *
         * private fun computeValue() { ... }
         * ```
         */
        @Fast
        @Suppress("unused")
        inline fun ofBoolean(fieldGetter: () -> Byte, fieldSetter: (Byte) -> Unit, valueProvider: () -> Boolean): Boolean {
            val field = fieldGetter()
            if (field != UNINITIALIZED_BOOLEAN) return field.toBoolean()
            val value = valueProvider()
            return value.also { fieldSetter(value.toByte()) }
        }

        /**
         * 用于实现内存友好的布尔类型的懒加载属性。基于 `@Volatile` 和双重检查锁。
         *
         * 示例：
         *
         * ```
         * val value: Boolean
         *     get() = LazyValue.ofBoolean(this, { _value }, { _value = it }) { computeValue() }
         * @Volatile private var _value = LazyValue.UNINITIALIZED_BOOLEAN
         *
         * private fun computeValue() { ... }
         * ```
         */
        @Fast
        inline fun ofBoolean(lock: Any, fieldGetter: () -> Byte, fieldSetter: (Byte) -> Unit, valueProvider: () -> Boolean): Boolean {
            val field = fieldGetter()
            if (field != UNINITIALIZED_BOOLEAN) return field.toBoolean()
            synchronized(lock) {
                val field = fieldGetter()
                if (field != UNINITIALIZED_BOOLEAN) return field.toBoolean()
                val value = valueProvider()
                return value.also { fieldSetter(value.toByte()) }
            }
        }

        /**
         * 用于实现内存友好的懒加载属性。基于 `@Volatile`。
         *
         * 示例：
         *
         * ```
         * val value: Value?
         *     get() = LazyValue.of({ _value }, { _value = it }) { computeValue() }
         * @Volatile private var _value = LazyValue.UNINITIALIZED
         *
         * private fun computeValue() { ... }
         * ```
         */
        @Fast
        inline fun <T> of(fieldGetter: () -> Any?, fieldSetter: (T) -> Unit, valueProvider: () -> T): T {
            val field = fieldGetter()
            @Suppress("UNCHECKED_CAST")
            if (field !== UNINITIALIZED) return field as T
            val value = valueProvider()
            return value.also { fieldSetter(value) }
        }

        /**
         * 用于实现内存友好的懒加载属性。基于 `@Volatile` 和双重检查锁。
         *
         * 示例：
         *
         * ```
         * val value: Value?
         *     get() = LazyValue.of(lock, { _value }, { _value = it }) { computeValue() }
         * @Volatile private var _value = LazyValue.UNINITIALIZED
         *
         * private fun computeValue() { ... }
         * ```
         */
        @Fast
        inline fun <T> of(lock: Any, fieldGetter: () -> Any?, fieldSetter: (T) -> Unit, valueProvider: () -> T): T {
            val field = fieldGetter()
            @Suppress("UNCHECKED_CAST")
            if (field !== UNINITIALIZED) return field as T
            synchronized(lock) {
                val field = fieldGetter()
                @Suppress("UNCHECKED_CAST")
                if (field !== UNINITIALIZED) return field as T
                val value = valueProvider()
                return value.also { fieldSetter(value) }
            }
        }
    }
}
