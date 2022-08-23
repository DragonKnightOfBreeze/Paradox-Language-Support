package icu.windea.pls.core.selector

class ChainedParadoxSelector<T>(
	private val baseComparator: Comparator<T>? = null
) : ParadoxSelector<T> {
	val selectors = mutableListOf<ParadoxSelector<T>>()
	
	var defaultValue: T? = null
	var defaultValuePriority = 0
	
	override fun select(result: T): Boolean {
		if(selectors.isEmpty()) return super.select(result)
		var finalSelectResult = true
		var finalSelectDefaultResult = true
		var finalDefaultValuePriority = 0
		for(selector in selectors) {
			val selectResult = selector.select(result)
			finalSelectResult = finalSelectResult && selectResult
			if(selectResult) finalDefaultValuePriority++
			finalSelectDefaultResult = finalSelectDefaultResult && (selectResult || selector.selectAll(result))
		}
		if(finalSelectDefaultResult && defaultValuePriority < finalDefaultValuePriority){
			defaultValue = result
			defaultValuePriority = finalDefaultValuePriority
		}
		return finalSelectResult
	}
	
	override fun selectAll(result: T): Boolean {
		if(selectors.isEmpty()) return super.selectAll(result)
		var finalSelectAllResult = true
		for(selector in selectors) {
			val selectAllResult = selector.selectAll(result)
			finalSelectAllResult = finalSelectAllResult && selectAllResult
		}
		return finalSelectAllResult
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
		return comparator?.thenComparing { a, b ->
			if(a == b) 0 else 1
		}
	}
}