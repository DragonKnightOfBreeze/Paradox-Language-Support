package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxSystemLinkExpressionNode (
	override val text: String,
	override val rangeInExpression: TextRange,
	val config: CwtSystemLinkConfig
) : ParadoxScopeExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.SYSTEM_LINK_KEY
	
	override fun getReference(element: ParadoxScriptStringExpressionElement): Reference {
		return Reference(element, rangeInExpression, config.pointer.element)
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxSystemLinkExpressionNode? {
			val config = configGroup.systemLinks.get(text)
				?: return null
			return ParadoxSystemLinkExpressionNode(text, textRange, config)
		}
	}
	
	class Reference(
		element: ParadoxScriptStringExpressionElement,
		rangeInElement: TextRange,
		val resolved: CwtProperty?
	) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
		override fun handleElementRename(newElementName: String): ParadoxScriptStringExpressionElement {
			throw IncorrectOperationException() //不允许重命名
		}
		
		override fun resolve() = resolved
	}
}
