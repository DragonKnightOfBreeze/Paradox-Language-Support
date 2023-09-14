package icu.windea.pls.model

import icu.windea.pls.config.expression.*
import icu.windea.pls.core.annotations.api.*

data class ParadoxDefinitionRelatedImageInfo(
	val key: String,
	val locationExpression: CwtImageLocationExpression,
	val required: Boolean = false,
	val primary: Boolean = false
) {
	@InferApi
	val primaryByInference: Boolean = key.equals("icon", true)
}
