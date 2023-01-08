package icu.windea.pls.core.model

import icu.windea.pls.config.cwt.expression.*

data class ParadoxDefinitionRelatedImageInfo(
	val name: String,
	val locationExpression: CwtImageLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
)
