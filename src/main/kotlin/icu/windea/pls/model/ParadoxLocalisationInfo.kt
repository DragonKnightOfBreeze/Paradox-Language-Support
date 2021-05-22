package icu.windea.pls.model

data class ParadoxLocalisationInfo(
	val name:String,
	val keyName:String,
	val required:Boolean = false,
	val primary:Boolean = false
)