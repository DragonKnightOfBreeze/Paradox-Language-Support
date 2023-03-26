package icu.windea.pls.core.expression

import com.intellij.openapi.progress.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
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
) : AbstractExpression(text), ParadoxDataExpression

object BlockParadoxDataExpression: AbstractExpression(PlsConstants.blockFolder), ParadoxDataExpression {
	override val type: ParadoxDataType = ParadoxDataType.BlockType
	override val text: String  = PlsConstants.blockFolder
	override val quoted: Boolean = false
	override val isKey: Boolean = false
}

object UnknownParadoxDataExpression: AbstractExpression(PlsConstants.unknownString), ParadoxDataExpression {
	override val type: ParadoxDataType = ParadoxDataType.UnknownType
	override val text: String  = PlsConstants.unknownString
	override val quoted: Boolean = false
	override val isKey: Boolean = false
}

fun Resolver.resolve(element: ParadoxScriptExpressionElement, matchType: Int = CwtConfigMatchType.DEFAULT): ParadoxDataExpression {
	return when {
		element is ParadoxScriptScriptedVariableReference -> {
			ProgressManager.checkCanceled() //这是必要的
			val valueElement = when {
				matchType == CwtConfigMatchType.STATIC -> return UnknownParadoxDataExpression 
				else -> element.referenceValue ?: return UnknownParadoxDataExpression
			}
			ParadoxDataExpressionImpl(valueElement.value, valueElement.type, valueElement.text.isLeftQuoted(), false)
		}
		element.type == ParadoxDataType.BlockType -> {
			BlockParadoxDataExpression
		}
		else -> {
			val isKey = element is ParadoxScriptPropertyKey
			ParadoxDataExpressionImpl(element.value, element.type, element.text.isLeftQuoted(), isKey)
		}
	}
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
