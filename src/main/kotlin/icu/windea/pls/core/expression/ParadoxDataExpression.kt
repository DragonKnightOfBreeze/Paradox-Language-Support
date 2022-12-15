package icu.windea.pls.core.expression

import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxDataExpression.*
import icu.windea.pls.script.psi.*

interface ParadoxDataExpression : ParadoxExpression {
	val type: ParadoxDataType
	
	companion object Resolver
}

class ParadoxDataExpressionImpl(
	override val text: String,
	override val type: ParadoxDataType,
	override val quoted: Boolean,
	override val isKey: Boolean?
) : AbstractExpression(text), ParadoxDataExpression {
	companion object Resolver
}

fun Resolver.resolve(element: ParadoxScriptPropertyKey): ParadoxDataExpression {
	return ParadoxDataExpressionImpl(element.value, element.getType, element.text.isLeftQuoted(), true)
}

fun Resolver.resolve(element: ParadoxScriptValue): ParadoxDataExpression {
	return ParadoxDataExpressionImpl(element.value, element.getType, element.text.isLeftQuoted(), false)
}

fun Resolver.resolve(value: String, isQuoted: Boolean, isKey: Boolean? = null): ParadoxDataExpression {
	val expressionType = ParadoxDataType.resolve(value)
	return ParadoxDataExpressionImpl(value, expressionType, isQuoted, isKey)
}

fun Resolver.resolve(text: String, isKey: Boolean? = null): ParadoxDataExpression {
	val value = text.unquote()
	val expressionType = ParadoxDataType.resolve(text)
	val quoted = text.isLeftQuoted()
	return ParadoxDataExpressionImpl(value, expressionType, quoted, isKey)
}
