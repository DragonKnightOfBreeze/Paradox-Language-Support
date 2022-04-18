package icu.windea.pls.core

data class ParadoxRelatedLocalisationInfo(
	val name: String,
	val keyName: String,
	val required: Boolean = false,
	val primary: Boolean = false
)