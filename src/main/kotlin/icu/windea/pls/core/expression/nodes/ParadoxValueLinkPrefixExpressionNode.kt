package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxValueLinkPrefixExpressionNode (
	override val text: String,
	override val rangeInExpression: TextRange,
	override val linkConfigs: List<CwtLinkConfig>
) : ParadoxLinkPrefixExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.VALUE_LINK_PREFIX_KEY
	
	override fun getReference(element: ParadoxScriptStringExpressionElement): Reference {
		return Reference(element, rangeInExpression, linkConfigs)
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxValueLinkPrefixExpressionNode {
			return ParadoxValueLinkPrefixExpressionNode(text, textRange, linkConfigs)
		}
	}
	
	class Reference(
		element: ParadoxScriptStringExpressionElement,
		rangeInElement: TextRange,
		val linkConfigs: List<CwtLinkConfig>
	) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
		override fun handleElementRename(newElementName: String): ParadoxScriptStringExpressionElement {
			throw IncorrectOperationException() //不允许重命名
		}
		
		override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
			return linkConfigs.mapNotNull { it.pointer.element }.mapToArray { PsiElementResolveResult(it) }
		}
	}
}
