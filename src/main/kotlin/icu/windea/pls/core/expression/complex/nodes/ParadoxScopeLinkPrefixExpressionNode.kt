package icu.windea.pls.core.expression.complex.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScopeLinkPrefixExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val linkConfigs: List<CwtLinkConfig>
) : ParadoxLinkPrefixExpressionNode {
    override fun getAttributesKey() = ParadoxScriptAttributesKeys.SCOPE_LINK_PREFIX_KEY
    
    override fun getReference(element: ParadoxScriptStringExpressionElement): Reference {
        return Reference(element, rangeInExpression, linkConfigs)
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxScopeLinkPrefixExpressionNode {
            return ParadoxScopeLinkPrefixExpressionNode(text, textRange, linkConfigs)
        }
    }
    
    class Reference(
        element: ParadoxScriptStringExpressionElement,
        rangeInElement: TextRange,
        val linkConfigs: List<CwtLinkConfig>
    ) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
        override fun handleElementRename(newElementName: String): PsiElement {
            throw IncorrectOperationException() //cannot rename cwt config
        }
        
        override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
            return linkConfigs.mapNotNull { it.pointer.element }.mapToArray { PsiElementResolveResult(it) }
        }
    }
}