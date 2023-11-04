package icu.windea.pls.config.settings

data class CwtPostfixTemplateSettings(
	override val id: String,
	val key: String,
	val example: String?,
	val variables: Map<String, String>, //variableName - defaultValue
	val expression: String
): CwtSettings
