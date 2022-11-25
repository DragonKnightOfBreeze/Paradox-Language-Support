package icu.windea.pls.script.exp

import icu.windea.pls.core.expression.*

interface ParadoxExpression : Expression {
	val text: String
	val quoted: Boolean
	val isKey: Boolean?
}
