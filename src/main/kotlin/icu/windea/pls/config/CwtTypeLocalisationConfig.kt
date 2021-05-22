package icu.windea.pls.config

/**
 * @property required (option) value
 * @property primary (option) value
 */
data class CwtTypeLocalisationConfig(
	val name: String,
	val expression: String,
	var required: Boolean = false,
	var primary: Boolean = false
)

