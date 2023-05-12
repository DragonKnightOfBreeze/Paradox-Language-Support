package icu.windea.pls.lang.model

data class Occurrence(
	var actual: Int,
	var min: Int?,
	var max: Int?,
	val relaxMin: Boolean = false
) {
	var minDefine: String? = null
	var maxDefine: String? = null
}

