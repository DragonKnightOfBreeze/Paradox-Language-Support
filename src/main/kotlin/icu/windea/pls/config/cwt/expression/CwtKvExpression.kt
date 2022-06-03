package icu.windea.pls.config.cwt.expression

interface CwtKvExpression : CwtExpression {
	val type: CwtDataType
	val value: String?
	val extraValue: Any?
}