package icu.windea.pls.core.expression

import icu.windea.pls.core.expression.*

interface ParadoxScriptExpression : ParadoxExpression {
	val quoted: Boolean
	val isKey: Boolean?
}
