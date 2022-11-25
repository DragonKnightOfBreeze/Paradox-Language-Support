package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	val configGroup: CwtConfigGroup
) : ParadoxScriptExpressionNode {
	override fun getAttributesKey(): TextAttributesKey? {
		if(text.isEmpty()) return null
		if(text.isParameterAwareExpression()) return null
		return ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
	}
	
	override fun getUnresolvedError(element: ParadoxScriptExpressionElement): ParadoxExpressionError? {
		if(nodes.isNotEmpty()) return null
		if(text.isEmpty()) return null
		if(text.isParameterAwareExpression()) return null
		//排除可解析的情况
		if(getReference(element).canResolve()) return null
		return ParadoxUnresolvedScriptValueExpressionError(rangeInExpression, PlsBundle.message("script.expression.unresolvedScriptValue", text))
	}
	
	override fun getReference(element: ParadoxScriptExpressionElement): Reference? {
		if(text.isEmpty()) return null
		if(text.isParameterAwareExpression()) return null
		return Reference(element, rangeInExpression, text, configGroup)
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScriptValueExpressionNode {
			return ParadoxScriptValueExpressionNode(text, textRange, configGroup)
		}
	}
	
	class Reference(
		element: ParadoxScriptExpressionElement,
		rangeInElement: TextRange,
		private val name: String,
		private val configGroup: CwtConfigGroup
	) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement), SmartPsiReference {
		override fun handleElementRename(newElementName: String): PsiElement {
			return element.setValue(rangeInElement.replace(element.value, newElementName))
		}
		
		override fun resolve(): PsiElement? {
			return resolve(true)
		}
		
		override fun resolve(exact: Boolean): PsiElement? {
			val gameType = configGroup.gameType
			val project = configGroup.project
			val typeExpression = "script_value"
			val selector = definitionSelector().gameType(gameType).preferRootFrom(element, exact)
			return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).find()
		}
		
		override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
			val gameType = configGroup.gameType
			val project = configGroup.project
			val typeExpression = "script_value"
			val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
			return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).findAll()
				.mapToArray { PsiElementResolveResult(it) }
		}
	}
}
