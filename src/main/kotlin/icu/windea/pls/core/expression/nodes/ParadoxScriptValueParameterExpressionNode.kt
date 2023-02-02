package icu.windea.pls.core.expression.nodes

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.core.component.*
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
	
	override fun getReference(element: ParadoxScriptStringExpressionElement): Reference? {
		if(scriptValueNode == null) return null
		if(text.isEmpty()) return null
		val reference = scriptValueNode.getReference(element)
		if(reference == null || !reference.canResolve()) return null //skip if script value cannot be resolved
		return Reference(element, rangeInExpression, scriptValueNode.text, text, configGroup)
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, scriptValueNode: ParadoxScriptValueExpressionNode?, configGroup: CwtConfigGroup): ParadoxScriptValueParameterExpressionNode {
			return ParadoxScriptValueParameterExpressionNode(text, textRange, scriptValueNode, configGroup)
		}
	}
	
	class Reference(
		element: ParadoxScriptStringExpressionElement,
		rangeInElement: TextRange,
		val scriptValueName: String,
		val parameterName: String,
		val configGroup: CwtConfigGroup
	) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
		override fun handleElementRename(newElementName: String): PsiElement {
			return element.setValue(rangeInElement.replace(element.value, newElementName))
		}
		
		override fun resolve(): PsiElement? {
			//NOTE 这里不使用 icu.windea.pls.config.core.component.ParadoxParameterResolver
			val element = element
			val name = parameterName
			val contextName = "definition@script_value"
			val definitionName = scriptValueName
			val definitionTypes = listOf("script_value")
			val readWriteAccess = ReadWriteAccessDetector.Access.Write
			val gameType = configGroup.gameType ?: return null
			val project = configGroup.project
			val result = ParadoxParameterElement(element, name, contextName, readWriteAccess, gameType, project)
			result.putUserData(ParadoxDefinitionParameterResolver.definitionNameKey, definitionName)
			result.putUserData(ParadoxDefinitionParameterResolver.definitionTypesKey, definitionTypes)
			return result
		}
	}
}
