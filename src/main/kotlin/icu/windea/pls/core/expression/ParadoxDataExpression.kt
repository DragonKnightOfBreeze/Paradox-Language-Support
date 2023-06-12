package icu.windea.pls.core.expression

import com.intellij.openapi.progress.*
import com.intellij.util.BitUtil
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxDataExpression.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

interface ParadoxDataExpression : ParadoxExpression {
	val type: ParadoxType
	
	companion object Resolver
}

fun ParadoxDataExpression.isParameterized() = type == ParadoxType.String && text.isParameterized()

class ParadoxDataExpressionImpl(
	override val text: String,
	override val type: ParadoxType,
	override val quoted: Boolean,
	override val isKey: Boolean?
) : AbstractExpression(text), ParadoxDataExpression

object BlockParadoxDataExpression: AbstractExpression(PlsConstants.blockFolder), ParadoxDataExpression {
	override val type: ParadoxType = ParadoxType.Block
	override val text: String  = PlsConstants.blockFolder
	override val quoted: Boolean = false
	override val isKey: Boolean = false
}

object UnknownParadoxDataExpression: AbstractExpression(PlsConstants.unknownString), ParadoxDataExpression {
	override val type: ParadoxType = ParadoxType.Unknown
	override val text: String  = PlsConstants.unknownString
	override val quoted: Boolean = false
	override val isKey: Boolean = false
}

fun Resolver.resolve(element: ParadoxScriptExpressionElement, matchOptions: Int = ParadoxConfigMatcher.Options.Default): ParadoxDataExpression {
	return when {
		element is ParadoxScriptBlock -> {
			BlockParadoxDataExpression
		}
		element is ParadoxScriptScriptedVariableReference -> {
			ProgressManager.checkCanceled()
			val valueElement = when {
				BitUtil.isSet(matchOptions, ParadoxConfigMatcher.Options.SkipIndex) -> return UnknownParadoxDataExpression
				else -> element.referenceValue ?: return UnknownParadoxDataExpression
			}
			ParadoxDataExpressionImpl(valueElement.value, valueElement.type, valueElement.text.isLeftQuoted(), false)
		}
		else -> {
			val isKey = element is ParadoxScriptPropertyKey
			ParadoxDataExpressionImpl(element.value, element.type, element.text.isLeftQuoted(), isKey)
		}
	}
}

fun Resolver.resolve(value: String, isQuoted: Boolean, isKey: Boolean? = null): ParadoxDataExpression {
	val expressionType = ParadoxType.resolve(value)
	return ParadoxDataExpressionImpl(value, expressionType, isQuoted, isKey)
}

fun Resolver.resolve(text: String, isKey: Boolean? = null): ParadoxDataExpression {
	val value = text.unquote()
	val expressionType = ParadoxType.resolve(text)
	val quoted = text.isLeftQuoted()
	return ParadoxDataExpressionImpl(value, expressionType, quoted, isKey)
}
