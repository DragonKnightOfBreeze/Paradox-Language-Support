package icu.windea.pls.config.setting

data class CwtFoldingSetting(
	override val id: String,
	val key: String?,
	val keys: List<String>?,
	val placeholder: String
) : CwtSetting

