package icu.windea.pls.util.selector

class ChainedParadoxSelector<T> : ParadoxSelector<T> {
	val selectors = mutableListOf<ParadoxSelector<T>>()
	
	override var defaultValue: T? = null
	
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
		var comparator: Comparator<T>? = null
		var isFirst = true
		selectors.forEach {
			if(isFirst) {
				comparator = it.comparator()
				isFirst = false
			} else {
				comparator = comparator?.thenComparing(it.comparator())
			}
		}
		return comparator
	}
}