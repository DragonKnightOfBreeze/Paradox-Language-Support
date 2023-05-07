package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.references.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueArgumentValueExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	val scriptValueNode: ParadoxScriptValueExpressionNode?,
	val argumentNode: ParadoxScriptValueArgumentExpressionNode?,
	val configGroup: CwtConfigGroup
) : ParadoxExpressionNode {
	override fun getAttributesKey(): TextAttributesKey {
		val type = ParadoxDataType.resolve(text)
		return when {
			type.isBooleanType() -> ParadoxScriptAttributesKeys.KEYWORD_KEY
			type.isFloatType() -> ParadoxScriptAttributesKeys.NUMBER_KEY
			else -> ParadoxScriptAttributesKeys.STRING_KEY
		}
	}
	
	override fun getReference(element: ParadoxScriptStringExpressionElement): ParadoxArgumentValuePsiReference? {
		if(!getSettings().inference.argumentValueConfig) return null
		if(scriptValueNode == null) return null
		if(text.isEmpty()) return null
		val reference = scriptValueNode.getReference(element)
		if(reference?.resolve() == null) return null //skip if script value cannot be resolved
		if(argumentNode == null) return null
		return ParadoxArgumentValuePsiReference(element, rangeInExpression) { argumentNode.getReference(element)?.resolve() }
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, scriptValueNode: ParadoxScriptValueExpressionNode?, parameterNode: ParadoxScriptValueArgumentExpressionNode?, configGroup: CwtConfigGroup): ParadoxScriptValueArgumentValueExpressionNode {
			return ParadoxScriptValueArgumentValueExpressionNode(text, textRange, scriptValueNode, parameterNode, configGroup)
		}
	}
}
