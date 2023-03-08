package icu.windea.pls.lang.model

import icu.windea.pls.config.expression.*

data class ParadoxDefinitionRelatedImageInfo(
	val name: String,
	val locationExpression: CwtImageLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
)
