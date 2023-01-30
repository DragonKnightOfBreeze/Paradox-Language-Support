package icu.windea.pls.core.util

import icu.windea.pls.config.cwt.expression.*

data class Occurrence(
	var actual: Int,
	val min: Int?,
	val max: Int?,
	val relaxMin: Boolean = false
)
