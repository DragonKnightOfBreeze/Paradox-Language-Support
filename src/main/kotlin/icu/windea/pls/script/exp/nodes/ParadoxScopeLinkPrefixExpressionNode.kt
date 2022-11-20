package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScopeLinkPrefixExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	val linkConfigs: List<CwtLinkConfig>
) : ParadoxScriptExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.SCOPE_LINK_PREFIX_KEY
	
	override fun getReference(element: ParadoxScriptExpressionElement) = Reference(element, rangeInExpression, linkConfigs)
	
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
			throw IncorrectOperationException() //不允许重命名
		}
		
		override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
			return linkConfigs.mapNotNull { it.pointer.element }.mapToArray { PsiElementResolveResult(it) }
		}
	}
}
