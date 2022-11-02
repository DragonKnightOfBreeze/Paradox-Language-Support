package icu.windea.pls.core.expression

import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxDataExpression.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*

interface ParadoxDataExpression : ParadoxExpression {
	val value: String
	val type: ParadoxDataType
	val quoted: Boolean
	val isKey: String?
	
	companion object Resolver
}

private class ParadoxDataExpressionImpl(
	override val value: String,
	override val type: ParadoxDataType,
	override val quoted: Boolean,
	override val isKey: Boolean?
) : AbstractExpression(value), ParadoxScriptExpression

fun Resolver.resolve(element: ParadoxScriptPropertyKey): ParadoxScriptExpression {
	return ParadoxDataExpressionImpl(element.value, element.expressionType, element.isQuoted(), true)
}

fun Resolver.resolve(element: ParadoxScriptValue): ParadoxScriptExpression {
	return ParadoxDataExpressionImpl(element.value, element.expressionType, element.isQuoted(), false)
}

fun Resolver.resolve(value: String, isQuoted: Boolean, isKey: Boolean? = null): ParadoxScriptExpression {
	val expressionType = ParadoxDataType.resolve(value)
	return ParadoxDataExpressionImpl(value, expressionType, isQuoted, isKey)
}

fun Resolver.resolve(text: String, isKey: Boolean? = null): ParadoxScriptExpression {
	val value = text.unquote()
	val expressionType = ParadoxDataType.resolve(text)
	val quoted = text.isQuoted()
	return ParadoxDataExpressionImpl(value, expressionType, quoted, isKey)
}