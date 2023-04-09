package icu.windea.pls.core.search.selector

/**
 * 用于指定如何选择需要查找的定义、本地化、文件等。
 */
interface ParadoxSelector<T> {
	/**
	 * 调用以下方法时，预先进行选择：
	 * * [icu.windea.pls.core.search.ParadoxQuery.find]
	 * * [icu.windea.pls.core.search.ParadoxQuery.findFirst]
	 */
	fun select(result: T): Boolean = true
	
	/**
	 * 调用以下方法时，预先进行选择：
	 * * [icu.windea.pls.core.search.ParadoxQuery.findAll]
	 * * [icu.windea.pls.core.search.ParadoxQuery.forEach]
	 */
	fun selectAll(result: T): Boolean = true
	
	/**
	 * 调用以下方法时，预先进行排序：
	 * * [icu.windea.pls.core.search.ParadoxQuery.findAll]
	 */
	fun comparator(): Comparator<T>? = null
}