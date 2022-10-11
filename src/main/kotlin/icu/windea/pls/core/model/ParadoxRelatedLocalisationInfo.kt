package icu.windea.pls.core.model

import icu.windea.pls.config.cwt.expression.*

data class ParadoxRelatedLocalisationInfo(
	val name: String,
	val locationExpression: CwtLocalisationLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
)