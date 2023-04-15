package icu.windea.pls.core.search.selector

import icu.windea.pls.core.search.*

/**
 * 用于指定如何选择需要查找的定义、本地化、文件等。
 *
 * @see ParadoxQuery
 */
interface ParadoxSelector<T> {
    /**
     * 调用以下方法时，预先进行选择：
     * * [ParadoxQuery.find]
     * * [ParadoxQuery.findFirst]
     */
    fun select(result: T): Boolean = true
    
    /**
     * 调用以下方法时，预先进行选择：
     * * [ParadoxQuery.findAll]
     * * [ParadoxQuery.forEach]
     */
    fun selectAll(result: T): Boolean = true
    
    /**
     * 调用以下方法时，预先进行排序：
     * * [ParadoxQuery.findAll]
     */
    fun comparator(): Comparator<T>? = null
}