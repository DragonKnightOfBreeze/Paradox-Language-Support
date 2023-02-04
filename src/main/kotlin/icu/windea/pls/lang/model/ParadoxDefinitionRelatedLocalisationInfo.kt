package icu.windea.pls.lang.model

import icu.windea.pls.config.cwt.expression.*

data class ParadoxDefinitionRelatedLocalisationInfo(
	val name: String,
	val locationExpression: CwtLocalisationLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
)