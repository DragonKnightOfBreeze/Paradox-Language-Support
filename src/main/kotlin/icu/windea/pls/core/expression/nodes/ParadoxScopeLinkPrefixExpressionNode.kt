package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScopeLinkPrefixExpressionNode (
	override val text: String,
	override val rangeInExpression: TextRange,
	val linkConfigs: List<CwtLinkConfig>
) : ParadoxExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.SCOPE_LINK_PREFIX_KEY
	
	override fun getReference(element: ParadoxScriptExpressionElement): Reference {
		return Reference(element, rangeInExpression, linkConfigs)
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxScopeLinkPrefixExpressionNode {
			return ParadoxScopeLinkPrefixExpressionNode(text, textRange, linkConfigs)
		}
	}
	
	class Reference(
		element: ParadoxScriptExpressionElement,
		rangeInElement: TextRange,
		private val linkConfigs: List<CwtLinkConfig>
	) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
		override fun handleElementRename(newElementName: String): ParadoxScriptExpressionElement {
			return element.setValue(rangeInElement.replace(element.value, newElementName))
		}
		
		override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
			return linkConfigs.mapNotNull { it.pointer.element }.mapToArray { PsiElementResolveResult(it) }
		}
	}
}
