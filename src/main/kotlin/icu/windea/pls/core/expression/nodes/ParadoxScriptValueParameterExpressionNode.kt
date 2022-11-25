package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueParameterExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange
) : ParadoxScriptExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.ARGUMENT_KEY
	
	override fun getReference(element: ParadoxScriptExpressionElement): Reference {
		return Reference(element, rangeInExpression)
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup) {
			TODO()
		}
	}
	
	class Reference(
		element: ParadoxScriptExpressionElement,
		rangeInElement: TextRange
	) : PsiReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
		override fun resolve(): PsiElement? {
			TODO("Not yet implemented")
		}
	}
}

