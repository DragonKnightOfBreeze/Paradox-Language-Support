package icu.windea.pls.cwt.expression

data class CwtValueExpression(
	override val type: CwtValueExpressionType,
	override val value: String? = null
): CwtExpression {
	
}