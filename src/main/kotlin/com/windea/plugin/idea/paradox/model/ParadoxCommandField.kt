package com.windea.plugin.idea.paradox.model

class ParadoxCommandField(data:Map<String,Any>){
	private val dataWithDefault = data.withDefault { key->
		when(key){
			"description" -> ""
			else -> null
		}
	}
	
	val name:String by dataWithDefault
	val description:String by dataWithDefault
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxCommandField && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}