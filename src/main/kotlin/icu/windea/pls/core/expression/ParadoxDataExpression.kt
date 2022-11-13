package icu.windea.pls.core.expression

import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxDataExpression.*
import icu.windea.pls.script.psi.*

class ParadoxDataExpression(
	override val value: String,
	val type: ParadoxDataType,
	override val quoted: Boolean,
	override val isKey: Boolean?
) : AbstractExpression(value), ParadoxScriptExpression {
	companion object Resolver : ParadoxExpressionResolver
}

fun Resolver.resolve(element: ParadoxScriptPropertyKey): ParadoxScriptExpression {
	return ParadoxDataExpression(element.value, element.expressionType, element.isQuoted(), true)
}

fun Resolver.resolve(element: ParadoxScriptValue): ParadoxScriptExpression {
	return ParadoxDataExpression(element.value, element.expressionType, element.isQuoted(), false)
}

fun Resolver.resolve(value: String, isQuoted: Boolean, isKey: Boolean? = null): ParadoxScriptExpression {
	val expressionType = ParadoxDataType.resolve(value)
	return ParadoxDataExpression(value, expressionType, isQuoted, isKey)
}

fun Resolver.resolve(text: String, isKey: Boolean? = null): ParadoxScriptExpression {
	val value = text.unquote()
	val expressionType = ParadoxDataType.resolve(text)
	val quoted = text.isQuoted()
	return ParadoxDataExpression(value, expressionType, quoted, isKey)
}