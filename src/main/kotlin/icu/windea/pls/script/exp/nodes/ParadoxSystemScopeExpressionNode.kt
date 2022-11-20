package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.highlighter.*

class ParadoxSystemScopeExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	val config: ParadoxSystemScopeConfig
) : ParadoxScriptExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.SYSTEM_SCOPE_KEY
	
	override fun getReference(element: PsiElement) = Reference(element, rangeInExpression, config.pointer.element)
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxSystemScopeExpressionNode? {
			val config = getInternalConfig(configGroup.project).systemScopeMap.get(text)
				?: return null
			return ParadoxSystemScopeExpressionNode(text, textRange, config)
		}
	}
	
	class Reference(
		element: PsiElement,
		rangeInElement: TextRange,
		private val resolved: CwtProperty?
	) : PsiReferenceBase<PsiElement>(element, rangeInElement) {
		override fun handleElementRename(newElementName: String) = throw IncorrectOperationException()
		
		override fun resolve() = resolved
	}
}
