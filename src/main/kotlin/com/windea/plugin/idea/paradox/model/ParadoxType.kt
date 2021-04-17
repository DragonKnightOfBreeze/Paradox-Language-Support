package com.windea.plugin.idea.paradox.model

data class ParadoxType(
	val name:String,
	val aliases:List<String> = emptyList()
){
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxType && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}