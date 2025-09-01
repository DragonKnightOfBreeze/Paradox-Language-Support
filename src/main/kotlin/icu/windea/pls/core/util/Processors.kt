package icu.windea.pls.core.util

import com.intellij.util.Processor

/**
 * `com.intellij.util.Processor` 的常用实现与工厂方法。
 */
object Processors {
    /**
     * 查找第一个满足条件的元素的处理器。
     *
     * 当 `accept(e)` 返回 true 时，保存为 [result] 并停止处理（返回 false）。
     */
    open class FindProcessor<T> : Processor<T> {
        var result: T? = null

        override fun process(e: T): Boolean {
            if (accept(e)) {
                result = e
                return false
            }
            return true
        }

        /** 是否接受该元素。默认总为 true。 */
        protected open fun accept(e: T): Boolean {
            return true
        }
    }

    /**
     * 收集满足条件的元素到给定集合的处理器。
     */
    open class CollectProcessor<T, C : MutableCollection<T>>(val collection: C) : Processor<T> {
        override fun process(e: T): Boolean {
            if (accept(e)) {
                collection.add(e)
            }
            return true
        }

        /** 是否接受该元素。默认总为 true。 */
        protected open fun accept(e: T): Boolean {
            return true
        }
    }

    /** 创建一个查找处理器。 */
    fun <T> find(): FindProcessor<T> {
        return FindProcessor()
    }

    /** 创建一个带过滤条件的查找处理器。 */
    fun <T> find(filter: (T) -> Boolean): FindProcessor<T> {
        return object : FindProcessor<T>() {
            override fun accept(e: T): Boolean {
                return filter(e)
            }
        }
    }

    /** 创建一个收集处理器。 */
    fun <T, C : MutableCollection<T>> collect(collection: C): CollectProcessor<T, C> {
        return CollectProcessor(collection)
    }

    /** 创建一个带过滤条件的收集处理器。 */
    fun <T, C : MutableCollection<T>> collect(collection: C, filter: (T) -> Boolean): CollectProcessor<T, C> {
        return object : CollectProcessor<T, C>(collection) {
            override fun accept(e: T): Boolean {
                return filter(e)
            }
        }
    }
}
