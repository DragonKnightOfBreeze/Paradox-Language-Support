package icu.windea.pls.core

/**
 * @property location 即本地化的键（localisationKey）。
 */
data class ParadoxRelatedLocalisationInfo(
	val key: String,
	val location: String,
	val required: Boolean = false,
	val primary: Boolean = false
)