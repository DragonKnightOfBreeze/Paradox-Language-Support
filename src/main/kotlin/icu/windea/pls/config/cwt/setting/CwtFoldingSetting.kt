package icu.windea.pls.config.cwt.setting

data class CwtFoldingSetting(
	val name: String,
	val key: String?,
	val keys: List<String>?,
	val placeholder: String
) : CwtSetting

