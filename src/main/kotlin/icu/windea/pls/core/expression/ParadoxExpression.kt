package icu.windea.pls.core.expression

interface ParadoxExpression : Expression {
	val value: String
	val type: ParadoxExpressionType
}

interface ParadoxExpressionResolver