package icu.windea.pls.lang.search.selector

import icu.windea.pls.lang.search.*

/**
 * 用于指定如何选择需要查询的定义、本地化、文件等。
 *
 * @see ParadoxQuery
 */
interface ParadoxSelector<T> {
    /**
     * 调用以下方法时，将会预先进行选择：
     * * [ParadoxQuery.find]
     * * [ParadoxQuery.findFirst]
     */
    fun select(target: T): Boolean = true
    
    /**
     * 调用以下方法时，将会预先进行选择：
     * * [ParadoxQuery.findAll]
     * * [ParadoxQuery.forEach]
     */
    fun selectAll(target: T): Boolean = true
    
    /**
     * 调用以下方法时，将会在最后进行进一步的处理：
     * * [ParadoxQuery.findAll]
     * * [ParadoxQuery.forEach]
     */
    fun postHandle(targets: Set<T>): Set<T> = targets
    
    /**
     * 调用以下方法时，将会进行排序：
     * * [ParadoxQuery.findAll]
     * * [ParadoxQuery.forEach]
     */
    fun comparator(): Comparator<T>? = null
}