package icu.windea.pls.model

import icu.windea.pls.config.cwt.expression.*

data class ParadoxRelatedImagesInfo(
	val name: String,
	val location: CwtImageLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
)