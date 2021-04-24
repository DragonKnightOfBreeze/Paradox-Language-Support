package com.windea.plugin.idea.pls.model

class ParadoxLocale(data:Map<String,Any>) {
	val name:String by data
	val description:String by data
	val popupText = "$name - $description"
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxLocale && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}