package icu.windea.pls.lang.model

import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.cwt.expression.*

data class ParadoxDefinitionRelatedImageInfo(
	val key: String,
	val locationExpression: CwtImageLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
) {
	@InferApi
	val primaryByInference: Boolean = key.equals("icon", true)
}
