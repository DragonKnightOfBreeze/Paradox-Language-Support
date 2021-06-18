package icu.windea.pls.config

/**
 * @property required (option) required
 * @property primary (option) primary
 */
data class CwtTypeLocalisationConfig(
	val name: String,
	val expression: String,
	val required: Boolean = false,
	val primary: Boolean = false
)

