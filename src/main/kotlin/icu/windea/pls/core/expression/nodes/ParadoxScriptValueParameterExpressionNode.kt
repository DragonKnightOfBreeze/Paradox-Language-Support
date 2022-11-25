package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueParameterExpressionNode (
	override val text: String,
	override val rangeInExpression: TextRange,
	val scriptValueName: String?,
	val configGroup: CwtConfigGroup
) : ParadoxScriptExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.ARGUMENT_KEY
	
	override fun getReference(element: ParadoxScriptExpressionElement): Reference? {
		if(scriptValueName == null) return null
		return Reference(element, rangeInExpression, scriptValueName, text, configGroup)
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, scriptValueName: String?, configGroup: CwtConfigGroup): ParadoxScriptValueParameterExpressionNode {
			return ParadoxScriptValueParameterExpressionNode(text, textRange, scriptValueName, configGroup)
		}
	}
	
	class Reference(
		element: ParadoxScriptExpressionElement,
		rangeInElement: TextRange,
		private val scriptValueName: String,
		private val parameterName: String,
		private val configGroup: CwtConfigGroup
	) : PsiReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
		override fun handleElementRename(newElementName: String): PsiElement {
			return element.setValue(rangeInElement.replace(element.value, newElementName))
		}
		
		override fun resolve(): PsiElement {
			val element = element
			val definitionType = "script_value"
			val project = configGroup.project
			val gameType = configGroup.gameType
			return ParadoxParameterElement(element, parameterName, scriptValueName, definitionType, project, gameType, false)
		}
	}
}
