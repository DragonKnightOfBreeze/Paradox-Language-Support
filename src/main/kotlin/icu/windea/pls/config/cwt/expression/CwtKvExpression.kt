package icu.windea.pls.config.cwt.expression

interface CwtKvExpression : CwtExpression {
	val type: CwtKvExpressionType
	val value: String?
	val extraValue: Any?
}