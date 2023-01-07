package icu.windea.pls.core.model

import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*

data class ParadoxRelatedImageInfo(
	val name: String,
	val locationExpression: CwtImageLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
)

data class ParadoxModifierInfo(
	val name: String,
	val config : CwtModifierConfig
)