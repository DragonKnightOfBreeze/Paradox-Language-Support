package icu.windea.pls.config.core.config

import icu.windea.pls.config.cwt.expression.*

data class ParadoxDefinitionRelatedImageInfo(
	val name: String,
	val locationExpression: CwtImageLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
)
