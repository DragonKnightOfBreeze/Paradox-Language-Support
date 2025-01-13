package icu.windea.pls.lang.search.selector

import icu.windea.pls.lang.search.*
import java.util.function.Function

/**
 * 用于指定如何选择需要查询的目标（定义、本地化、文件等）。
 *
 * @see ParadoxQuery
 */
interface ParadoxSelector<T> {
    /**
     * 选择单个目标时是否选用。
     */
    fun selectOne(target: T): Boolean = true

    /**
     * 选择多个目标时是否选用。
     */
    fun select(target: T): Boolean = true

    /**
     * 选择多个目标时需要用到的键选择器。用于去重。
     */
    fun keySelector(): Function<T, Any?>? = null

    /**
     * 选择多个目标时需要使用到的比较器。用于排序。
     */
    fun comparator(): Comparator<T>? = null
}
