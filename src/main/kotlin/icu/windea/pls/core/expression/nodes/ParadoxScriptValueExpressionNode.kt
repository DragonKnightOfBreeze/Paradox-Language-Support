package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.collections.*
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
		if(text.isParameterAwareExpression()) return null
		return ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
	}
	
	override fun getReference(element: ParadoxScriptExpressionElement): Reference? {
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
			//重命名当前元素
			return element.setValue(newElementName)
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
