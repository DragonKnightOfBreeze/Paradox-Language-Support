package icu.windea.pls.core.ui

import icu.windea.pls.config.core.config.*

sealed interface ElementDescriptor{
	val name: String
	val editInTemplate: Boolean
	
	fun copyDescriptor(): ElementDescriptor
}

data class ValueDescriptor(
	override var name: String = ""
): ElementDescriptor {
	override val editInTemplate: Boolean get() = false
	
	override fun copyDescriptor(): ElementDescriptor = copy()
}

data class PropertyDescriptor(
	override var name: String = "",
	var separator: ParadoxSeparator = ParadoxSeparator.EQUAL,
	var value: String = "", 
	val constantValues: List<String> = emptyList() 
): ElementDescriptor {
	val constantValueArray = constantValues.toTypedArray()
	
	override val editInTemplate get() = value.isEmpty()
	
	override fun copyDescriptor(): ElementDescriptor = copy()
}