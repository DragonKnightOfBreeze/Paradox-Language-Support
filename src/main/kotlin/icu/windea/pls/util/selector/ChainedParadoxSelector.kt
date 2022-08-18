package icu.windea.pls.util.selector

class ChainedParadoxSelector<T>(
	private val baseComparator: Comparator<T>? = null
) : ParadoxSelector<T> {
	val selectors = mutableListOf<ParadoxSelector<T>>()
	
	//TODO 处理默认值的链式传递
	//TODO 处理置顶逻辑
	
	var defaultValue: T? = null
	
	override fun select(result: T): Boolean {
		if(selectors.isEmpty()) return super.select(result)
		if(defaultValue == null && selectDefault(result)) defaultValue = result
		return selectors.all { it.select(result) }
	}
	
	override fun selectAll(result: T): Boolean {
		if(selectors.isEmpty()) return super.selectAll(result)
		return selectors.all { it.selectAll(result) }
	}
	
	override fun selectDefault(result: T): Boolean {
		if(selectors.isEmpty()) return super.selectDefault(result)
		return selectors.all { it.selectDefault(result) }
	}
	
	override fun comparator(): Comparator<T>? {
		if(selectors.isEmpty()) return super.comparator()
		var comparator: Comparator<T>? = baseComparator
		selectors.forEach {
			if(comparator == null) {
				comparator = it.comparator()
			} else {
				comparator = comparator?.thenComparing(it.comparator())
			}
		}
		//最终使用的排序器需要将比较结果为0的项按照原有顺序进行排序，除非它们值相等
		return comparator?.thenComparator { a, b ->
			if(a == b) 0 else 1
		}
	}
}