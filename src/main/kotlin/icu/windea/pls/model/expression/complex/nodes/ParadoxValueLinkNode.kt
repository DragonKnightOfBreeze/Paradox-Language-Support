package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxValueLinkNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val config: CwtLinkConfig
) : ParadoxValueFieldNode {
    override fun getAttributesKey() = ParadoxScriptAttributesKeys.VALUE_LINK_VALUE_KEY
    
    override fun getReference(element: ParadoxScriptStringExpressionElement) = ParadoxScopeLinkNode.Reference(element, rangeInExpression, config)
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxValueLinkNode? {
            val config = configGroup.linksAsValueNotData.get(text)
                ?: return null
            return ParadoxValueLinkNode(text, textRange, config)
        }
    }
    
    class Reference(
        element: ParadoxScriptStringExpressionElement,
        rangeInElement: TextRange,
        val config: CwtLinkConfig
    ) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
        override fun handleElementRename(newElementName: String): PsiElement {
            throw IncorrectOperationException() //cannot rename cwt config
        }
        
        override fun resolve() = config.pointer.element
    }
}
