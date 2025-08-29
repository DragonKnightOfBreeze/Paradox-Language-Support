package icu.windea.pls.core.util

import com.intellij.util.Processor

object Processors {
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

    fun <T> find(): FindProcessor<T> {
        return FindProcessor()
    }

    fun <T> find(filter: (T) -> Boolean): FindProcessor<T> {
        return object : FindProcessor<T>() {
            override fun accept(e: T): Boolean {
                return filter(e)
            }
        }
    }

    fun <T, C : MutableCollection<T>> collect(collection: C): CollectProcessor<T, C> {
        return CollectProcessor(collection)
    }

    fun <T, C : MutableCollection<T>> collect(collection: C, filter: (T) -> Boolean): CollectProcessor<T, C> {
        return object : CollectProcessor<T, C>(collection) {
            override fun accept(e: T): Boolean {
                return filter(e)
            }
        }
    }
}
