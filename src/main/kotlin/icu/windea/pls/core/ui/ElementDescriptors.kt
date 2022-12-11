package icu.windea.pls.core.ui

import icu.windea.pls.core.model.*

sealed interface ElementDescriptor{
	val name: String
	
	val editInTemplate: Boolean
	
	fun copyDescriptor(): ElementDescriptor
}

data class PropertyDescriptor(
	override val name: String = "",
	var separator: ParadoxSeparator = ParadoxSeparator.EQUAL,
	var value: String = "", //with quotes
	val constantValues: List<String> = emptyList() 
): ElementDescriptor {
	val constantValueArray = constantValues.toTypedArray()
	
	override val editInTemplate get() = value.isEmpty()
	
	override fun copyDescriptor(): ElementDescriptor {
		return copy()
	}
}

data class ValueDescriptor(
	override val name: String = ""
): ElementDescriptor {
	override val editInTemplate get() = false
	
	override fun copyDescriptor(): ElementDescriptor {
		return copy()
	}
}

data class NewPropertyDescriptor(
	override var name: String = "",
	var separator: ParadoxSeparator = ParadoxSeparator.EQUAL,
	var value: String = "", //with quotes
) : ElementDescriptor {
	override val editInTemplate get() = value.isEmpty()
	
	override fun copyDescriptor(): ElementDescriptor {
		return copy()
	}
}