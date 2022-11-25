package icu.windea.pls.core.expression

import icu.windea.pls.core.*

interface ParadoxExpression : Expression {
	val text: String
	val quoted: Boolean
	val isKey: Boolean?
}
