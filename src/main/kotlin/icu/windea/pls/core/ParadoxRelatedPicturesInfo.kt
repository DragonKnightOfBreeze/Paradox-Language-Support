package icu.windea.pls.core

import icu.windea.pls.config.cwt.expression.*

data class ParadoxRelatedPicturesInfo(
	val name: String,
	val location: CwtPictureLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
)