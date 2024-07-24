package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.references.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.highlighter.*

class ParadoxValueLinkPrefixNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val linkConfigs: List<CwtLinkConfig>
) : ParadoxComplexExpressionNode.Base(), ParadoxLinkPrefixNode {
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return ParadoxScriptAttributesKeys.VALUE_LINK_PREFIX_KEY
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionHandler.getExpressionOffset(element))
        return Reference(element, rangeInElement, linkConfigs.mapNotNull { it.pointer.element })
    }
    
    class Reference(element: ParadoxExpressionElement, rangeInElement: TextRange, resolved: List<CwtProperty>) :
        PsiResolvedPolyVariantReference<CwtProperty>(element, rangeInElement, resolved)
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxValueLinkPrefixNode {
            return ParadoxValueLinkPrefixNode(text, textRange, linkConfigs)
        }
    }
}
