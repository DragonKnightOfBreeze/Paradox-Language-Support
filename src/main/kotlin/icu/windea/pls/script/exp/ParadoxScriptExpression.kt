package icu.windea.pls.script.exp

import icu.windea.pls.core.expression.*

interface ParadoxScriptExpression : Expression {
	val value: String
	val quoted: Boolean
	val isKey: Boolean?
}