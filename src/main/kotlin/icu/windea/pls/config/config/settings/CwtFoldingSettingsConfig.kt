package icu.windea.pls.config.config.settings

data class CwtFoldingSettingsConfig(
	override val id: String,
	val key: String?,
	val keys: List<String>?,
	val placeholder: String
) : CwtSettingsConfig

