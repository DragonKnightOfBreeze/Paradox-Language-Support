package icu.windea.pls.lang.cwt.setting

data class CwtFoldingSetting(
	override val id: String,
	val key: String?,
	val keys: List<String>?,
	val placeholder: String
) : CwtSetting

