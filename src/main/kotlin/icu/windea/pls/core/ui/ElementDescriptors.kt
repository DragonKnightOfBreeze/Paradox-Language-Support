package icu.windea.pls.core.ui

import icu.windea.pls.core.model.*

sealed interface ElementDescriptor{
	var checked: Boolean
	val name: String
}

data class PropertyDescriptor(
	override var checked: Boolean = true,
	override val name: String,
	var separator: ParadoxSeparator = ParadoxSeparator.EQUAL,
	var value: String = "", //with quotes
	val constantValues: List<String> = emptyList() 
): ElementDescriptor

data class ValueDescriptor(
	override var checked: Boolean = true,
	override val name: String
): ElementDescriptor