package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.script.highlighter.*

class ParadoxScriptValueParameterValueExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	val scriptValueName: String?,
	val parameterName: String?,
	configGroup: CwtConfigGroup
) : ParadoxExpressionNode {
	override fun getAttributesKey(): TextAttributesKey {
		val type = ParadoxDataType.resolve(text)
		return when {
			type.isBooleanType() -> ParadoxScriptAttributesKeys.KEYWORD_KEY
			type.isFloatType() -> ParadoxScriptAttributesKeys.NUMBER_KEY
			else -> ParadoxScriptAttributesKeys.STRING_KEY
		}
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, scriptValueName: String?, parameterName: String?, configGroup: CwtConfigGroup): ParadoxScriptValueParameterValueExpressionNode {
			return ParadoxScriptValueParameterValueExpressionNode(text, textRange, scriptValueName, parameterName, configGroup)
		}
	}
}
