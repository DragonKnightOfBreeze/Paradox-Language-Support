package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxValueLinkExpressionNode (
	override val text: String,
	override val rangeInExpression: TextRange,
	val config: CwtLinkConfig
) : ParadoxValueFieldExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.VALUE_LINK_VALUE_KEY
	
	override fun getReference(element: ParadoxScriptStringExpressionElement) = ParadoxScopeLinkExpressionNode.Reference(element, rangeInExpression, config)
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxValueLinkExpressionNode? {
			val config = configGroup.linksAsValueNotData.get(text)
				?: return null
			return ParadoxValueLinkExpressionNode(text, textRange, config)
		}
	}
	
	class Reference(
		element: ParadoxScriptStringExpressionElement,
		rangeInElement: TextRange,
		val config: CwtLinkConfig
	) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
		override fun handleElementRename(newElementName: String): ParadoxScriptStringExpressionElement {
			throw IncorrectOperationException() //不允许重命名
		}
		
		override fun resolve() = config.pointer.element
	}
}
