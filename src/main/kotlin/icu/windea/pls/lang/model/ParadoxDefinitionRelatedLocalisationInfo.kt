package icu.windea.pls.lang.model

import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.cwt.expression.*

data class ParadoxDefinitionRelatedLocalisationInfo(
	val key: String,
	val locationExpression: CwtLocalisationLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
) {
	@InferApi
	val primaryByInference: Boolean = key.equals("name", true) || key.equals("title", true)
}