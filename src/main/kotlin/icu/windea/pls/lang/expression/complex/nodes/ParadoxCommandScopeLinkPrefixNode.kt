package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.references.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.highlighter.*

class ParadoxCommandScopeLinkPrefixNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val linkConfigs: List<CwtLinkConfig>
) : ParadoxComplexExpressionNode.Base(), ParadoxLinkPrefixNode {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_LINK_PREFIX_KEY
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, linkConfigs.mapNotNull { it.pointer.element })
    }
    
    class Reference(element: PsiElement, rangeInElement: TextRange, resolved: List<CwtProperty>) :
        PsiResolvedPolyVariantReference<CwtProperty>(element, rangeInElement, resolved)
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandScopeLinkPrefixNode? {
            val configs = configGroup.linksAsScopeWithPrefixSorted.filter { it.prefix != null && text == it.prefix!! }
            if(configs.isEmpty()) return null
            return ParadoxCommandScopeLinkPrefixNode(text, textRange, configGroup, configs)
        }
    }
}
