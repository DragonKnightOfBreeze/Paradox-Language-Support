package icu.windea.pls.core.util

import com.intellij.util.Processor

/**
 * Processor 工具集。
 *
 * 提供通用的查找与收集处理器实现，便于与 IntelliJ 平台 API 交互时快速落地。
 */
object Processors {
    /**
     * 查找处理器：在处理到第一个满足条件的元素时终止迭代。
     *
     * 可通过覆写 [accept] 指定匹配条件；匹配成功后结果存入 [result] 并返回 `false` 停止迭代。
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

        protected open fun accept(e: T): Boolean {
            return true
        }
    }

    /**
     * 收集处理器：将满足条件的元素加入到给定集合 [collection]。
     *
     * 可通过覆写 [accept] 指定过滤条件；处理始终返回 `true` 以继续迭代。
     */
    open class CollectProcessor<T, C : MutableCollection<T>>(val collection: C) : Processor<T> {
        override fun process(e: T): Boolean {
            if (accept(e)) {
                collection.add(e)
            }
            return true
        }

        protected open fun accept(e: T): Boolean {
            return true
        }
    }

    /** 创建一个不带过滤条件的 [FindProcessor]。*/
    fun <T> find(): FindProcessor<T> {
        return FindProcessor()
    }

    /** 创建一个带过滤条件 [filter] 的 [FindProcessor]。*/
    fun <T> find(filter: (T) -> Boolean): FindProcessor<T> {
        return object : FindProcessor<T>() {
            override fun accept(e: T): Boolean {
                return filter(e)
            }
        }
    }

    /** 创建一个不带过滤条件的 [CollectProcessor]，将元素加入 [collection]。*/
    fun <T, C : MutableCollection<T>> collect(collection: C): CollectProcessor<T, C> {
        return CollectProcessor(collection)
    }

    /** 创建一个带过滤条件 [filter] 的 [CollectProcessor]。*/
    fun <T, C : MutableCollection<T>> collect(collection: C, filter: (T) -> Boolean): CollectProcessor<T, C> {
        return object : CollectProcessor<T, C>(collection) {
            override fun accept(e: T): Boolean {
                return filter(e)
            }
        }
    }
}
