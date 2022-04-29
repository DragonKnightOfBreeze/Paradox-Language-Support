package icu.windea.pls.core

data class ParadoxRelatedLocalisationInfo(
	val name: String,
	val location: String,
	val required: Boolean = false,
	val primary: Boolean = false
)

data class ParadoxRelatedPicturesInfo(
	val name: String,
	val location: String,
	val required: Boolean = false,
	val primary: Boolean = false
)