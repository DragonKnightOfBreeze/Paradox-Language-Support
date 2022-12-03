package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueParameterExpressionNode (
	override val text: String,
	override val rangeInExpression: TextRange,
	val scriptValueNode: ParadoxScriptValueExpressionNode?,
	val configGroup: CwtConfigGroup
) : ParadoxExpressionNode {
	override fun getAttributesKey(): TextAttributesKey? {
		if(text.isEmpty()) return null
		return ParadoxScriptAttributesKeys.ARGUMENT_KEY
	}
	
	override fun getReference(element: ParadoxScriptExpressionElement): Reference? {
		if(scriptValueNode == null) return null
		if(text.isEmpty()) return null
		if(!scriptValueNode.getReference(element).canResolve()) return null //skip if script value cannot be resolved
		return Reference(element, rangeInExpression, scriptValueNode.text, text, configGroup)
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, scriptValueNode: ParadoxScriptValueExpressionNode?, configGroup: CwtConfigGroup): ParadoxScriptValueParameterExpressionNode {
			return ParadoxScriptValueParameterExpressionNode(text, textRange, scriptValueNode, configGroup)
		}
	}
	
	class Reference(
		element: ParadoxScriptExpressionElement,
		rangeInElement: TextRange,
		val scriptValueName: String,
		val parameterName: String,
		val configGroup: CwtConfigGroup
	) : PsiReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
		override fun handleElementRename(newElementName: String): PsiElement {
			return element.setValue(rangeInElement.replace(element.value, newElementName))
		}
		
		override fun resolve(): PsiElement? {
			val element = element
			val definitionType = "script_value"
			val project = configGroup.project
			val gameType = configGroup.gameType ?: return null
			return ParadoxParameterElement(element, parameterName, scriptValueName, definitionType, project, gameType, false)
		}
	}
}
