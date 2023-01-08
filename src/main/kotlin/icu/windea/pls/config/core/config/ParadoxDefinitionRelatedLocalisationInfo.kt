package icu.windea.pls.config.core.config

import icu.windea.pls.config.cwt.expression.*

data class ParadoxDefinitionRelatedLocalisationInfo(
	val name: String,
	val locationExpression: CwtLocalisationLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
)