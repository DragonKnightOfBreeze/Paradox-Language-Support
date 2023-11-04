package icu.windea.pls.config.settings

data class CwtFoldingSettings(
	override val id: String,
	val key: String?,
	val keys: List<String>?,
	val placeholder: String
) : CwtSettings

