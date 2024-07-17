package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.highlighter.*

class ParadoxSystemLinkNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val config: CwtSystemLinkConfig
) : ParadoxComplexExpressionNode.Base(), ParadoxScopeFieldNode {
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return ParadoxScriptAttributesKeys.SYSTEM_LINK_KEY
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionHandler.getExpressionOffset(element))
        return Reference(element, rangeInElement, config.pointer.element)
    }
    
    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        val resolved: CwtProperty?
    ) : PsiReferenceBase<ParadoxExpressionElement>(element, rangeInElement) {
        override fun handleElementRename(newElementName: String): PsiElement {
            throw IncorrectOperationException() //cannot rename cwt config
        }
        
        override fun resolve() = resolved
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxSystemLinkNode? {
            val config = configGroup.systemLinks.get(text) ?: return null
            return ParadoxSystemLinkNode(text, textRange, config)
        }
    }
}
