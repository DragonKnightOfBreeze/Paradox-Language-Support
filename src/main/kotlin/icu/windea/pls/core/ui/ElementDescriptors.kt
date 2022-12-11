package icu.windea.pls.core.ui

import icu.windea.pls.core.model.*

sealed interface ElementDescriptor{
	val name: String
}

data class PropertyDescriptor(
	override val name: String,
	var separator: ParadoxSeparator = ParadoxSeparator.EQUAL,
	var value: String = "", //with quotes
	val constantValues: List<String> = emptyList() 
): ElementDescriptor

data class ValueDescriptor(
	override val name: String
): ElementDescriptor