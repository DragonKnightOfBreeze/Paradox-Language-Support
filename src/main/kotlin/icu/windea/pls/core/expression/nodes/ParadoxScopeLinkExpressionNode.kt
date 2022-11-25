package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScopeLinkExpressionNode (
	override val text: String,
	override val rangeInExpression: TextRange,
	val config: CwtLinkConfig
) : ParadoxScopeExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.SCOPE_KEY
	
	override fun getReference(element: ParadoxScriptExpressionElement): Reference {
		return Reference(element, rangeInExpression, config)
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeLinkExpressionNode? {
			val config = configGroup.linksAsScopeNotData.get(text)
				?: return null
			return ParadoxScopeLinkExpressionNode(text, textRange, config)
		}
	}
	
	class Reference(
		element: ParadoxScriptExpressionElement,
		rangeInElement: TextRange,
		private val config: CwtLinkConfig
	) : PsiReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
		override fun handleElementRename(newElementName: String): ParadoxScriptExpressionElement {
			return element.setValue(rangeInElement.replace(element.value, newElementName))
		}
		
		override fun resolve() = config.pointer.element
	}
}
