package com.windea.plugin.idea.paradox.model

class ParadoxCommandScope(data:Map<String,Any>) {
	private val dataWithDefault = data.withDefault { key->
		when(key){
			"isPrimary" -> false
			"isSecondary" -> false
			else -> null
		}
	}
	
	val name:String by dataWithDefault
	val description: String by dataWithDefault
	val isPrimary:Boolean by dataWithDefault
	val isSecondary:Boolean by dataWithDefault
	
	override fun equals(other: Any?) = this === other || other is ParadoxCommandScope && name == other.name
	
	override fun hashCode() = name.hashCode()
	
	override fun toString() = name
}