package icu.windea.pls.core.util

@Suppress("unused")
object ProcessorFactory {
    /** 创建一个不带过滤条件的 [FindProcessor]。 */
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> find(): FindProcessor<T> {
        return FindProcessor()
    }

    /** 创建一个带过滤条件 [predicate] 的 [FindProcessor]。 */
    inline fun <T> find(crossinline predicate: (T) -> Boolean): FindProcessor<T> {
        return object : FindProcessor<T>() {
            override fun accept(e: T) = predicate(e)
        }
    }

    /** 创建一个不带过滤条件的 [CollectProcessor]，将元素加入 [collection]。 */
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T, C : MutableCollection<T>> collect(collection: C): CollectProcessor<T, C> {
        return CollectProcessor(collection)
    }

    /** 创建一个带过滤条件 [predicate] 的 [CollectProcessor]，将元素加入 [collection]。 */
    inline fun <T, C : MutableCollection<T>> collect(collection: C, crossinline predicate: (T) -> Boolean): CollectProcessor<T, C> {
        return object : CollectProcessor<T, C>(collection) {
            override fun accept(e: T) = predicate(e)
        }
    }

    /** 创建一个不带过滤条件的 [DuplicateProcessor]。 */
    fun <T> duplicate(): DuplicateProcessor<T> {
        return DuplicateProcessor()
    }

    /** 创建一个带过滤条件 [filter] 的 [DuplicateProcessor]。 */
    inline fun <T> duplicate(crossinline filter: (T) -> Boolean): DuplicateProcessor<T> {
        return object : DuplicateProcessor<T>() {
            override fun accept(e: T) = filter(e)
        }
    }

    /** 创建一个不带过滤条件的 [CollectProcessor]，将元素加入可变列表。 */
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> collect(): CollectProcessor<T, MutableList<T>> {
        return collect(mutableListOf())
    }

    /** 创建一个带过滤条件 [predicate] 的 [CollectProcessor]，将元素加入列表。 */
    inline fun <T> collect(crossinline predicate: (T) -> Boolean): CollectProcessor<T, MutableList<T>> {
        return collect(mutableListOf(), predicate)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> all(): AllProcessor<T> {
        return AllProcessor()
    }

    inline fun <T> all(crossinline predicate: (T) -> Boolean): AllProcessor<T> {
        return object : AllProcessor<T>() {
            override fun accept(e: T) = predicate(e)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> any(): AnyProcessor<T> {
        return AnyProcessor()
    }

    inline fun <T> any(crossinline predicate: (T) -> Boolean): AnyProcessor<T> {
        return object : AnyProcessor<T>() {
            override fun accept(e: T) = predicate(e)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> none(): NoneProcessor<T> {
        return NoneProcessor()
    }

    inline fun <T> none(crossinline predicate: (T) -> Boolean): NoneProcessor<T> {
        return object : NoneProcessor<T>() {
            override fun accept(e: T) = predicate(e)
        }
    }
}
