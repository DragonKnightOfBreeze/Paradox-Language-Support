package icu.windea.pls.config.cwt.expression

import icu.windea.pls.core.expression.*

interface CwtExpression : Expression

interface CwtExpressionResolver<T : CwtExpression>: ExpressionResolver<T> {
	fun resolve(expressionString: String): T
}