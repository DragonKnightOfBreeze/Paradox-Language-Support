package icu.windea.pls.core.util.values

import icu.windea.pls.core.EMPTY_OBJECT
import java.util.function.Supplier

/**
 * 可以延迟初始化的值（包装类）。
 *
 * 说明：
 * - 使用双重检查锁定（double-checked locking）保证初始化时的线程安全。
 * - 不同于 [Lazy]，不需要在声明时就指定初始化逻辑。
 */
class LazyValue<T> : Supplier<T?> {
    @Volatile private var _value: Any? = EMPTY_OBJECT

    @Suppress("UNCHECKED_CAST")
    var value: T?
        get() = if (isInitialized()) _value as? T else null
        set(value) {
            _value = value
        }

    override fun get(): T? = value

    fun isInitialized(): Boolean {
        return _value !== EMPTY_OBJECT
    }

    fun clear() {
        _value = EMPTY_OBJECT
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
        inline val UNINITIALIZED get() = EMPTY_OBJECT

        /**
         * 用于实现内存友好的懒加载属性。基于 `@Volatile`。
         *
         * 示例：
         *
         * ```
         * val value: Value?
         *     get() = LazyValue(_value) { computeValue().also {_value = it}  }
         * @Volatile private var _value: Any? = LazyValue.UNINITIALIZED
         *
         * private fun computeValue() { ... }
         * ```
         */
        @Suppress("UNCHECKED_CAST")
        inline operator fun <T> invoke(field: Any?, provider: () -> T?): T? {
            if (field !== EMPTY_OBJECT) return field as T
            return provider()
        }

        /**
         * 用于实现内存友好的懒加载属性。基于 `@Volatile` 和双重检查锁。
         *
         * 示例：
         *
         * ```
         * val value: Value?
         *     get() = LazyValue(this, { _value }) { computeValue().also {_value = it}  }
         * @Volatile private var _value: Any? = LazyValue.UNINITIALIZED
         *
         * private fun computeValue() { ... }
         * ```
         */
        @Suppress("UNCHECKED_CAST")
        inline operator fun <T> invoke(lock: Any, fieldProvider: () -> Any?, provider: () -> T?): T? {
            val field = fieldProvider()
            if (field !== EMPTY_OBJECT) return field as T
            synchronized(lock) {
                val field = fieldProvider()
                if (field !== EMPTY_OBJECT) return field as T
                return provider()
            }
        }
    }
}
