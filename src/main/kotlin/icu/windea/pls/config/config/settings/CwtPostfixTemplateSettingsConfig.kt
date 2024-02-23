package icu.windea.pls.config.config.settings

data class CwtPostfixTemplateSettingsConfig(
    override val id: String,
    val key: String,
    val example: String?,
    val variables: Map<String, String>, //variableName - defaultValue
    val expression: String
) : CwtSettingsConfig
