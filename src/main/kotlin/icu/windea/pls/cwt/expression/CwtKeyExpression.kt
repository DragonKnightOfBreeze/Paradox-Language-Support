package icu.windea.pls.cwt.expression

data class CwtKeyExpression(
	override val type: CwtKeyExpressionType,
	override val value: String? = null
): CwtExpression