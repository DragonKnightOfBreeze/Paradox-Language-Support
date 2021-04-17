package com.windea.plugin.idea.paradox.model

class ParadoxSequentialNumber(data:Map<String,Any>) {
	val name: String by data
	val description:String by data
	val placeholderText :String by data
	val popupText = "$name - $description"
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxSequentialNumber && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}