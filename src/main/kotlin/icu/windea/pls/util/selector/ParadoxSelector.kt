package icu.windea.pls.util.selector

/**
 * 用于指定如何选择需要查找的定义、本地化、文件等，尤其时当存在覆盖与重载的情况时。
 */
interface ParadoxSelector<T> {
	fun select(result: T): Boolean = true
	
	fun selectAll(result: T): Boolean = true
	
	fun selectDefault(result: T): Boolean = false
	
	fun comparator(): Comparator<T>? = null
}