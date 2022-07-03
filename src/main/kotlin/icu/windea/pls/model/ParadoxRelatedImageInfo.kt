package icu.windea.pls.model

import icu.windea.pls.config.cwt.expression.*

data class ParadoxRelatedImageInfo(
	val name: String,
	val locationExpression: CwtImageLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
)