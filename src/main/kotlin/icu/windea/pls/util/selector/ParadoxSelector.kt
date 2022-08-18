package icu.windea.pls.util.selector

/**
 * 用于指定如何选择（过滤和排序）需要查找的定义、本地化、文件等。
 */
interface ParadoxSelector<T> {
	fun select(result: T): Boolean = true
	
	fun selectAll(result: T): Boolean = true
	
	fun comparator(): Comparator<T>? = null
}