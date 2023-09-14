package icu.windea.pls.model

import icu.windea.pls.config.expression.*
import icu.windea.pls.core.annotations.api.*

data class ParadoxDefinitionRelatedLocalisationInfo(
	val key: String,
	val locationExpression: CwtLocalisationLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
) {
	@InferApi
	val primaryByInference: Boolean = key.equals("name", true) || key.equals("title", true)
}