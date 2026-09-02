package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.resolveElementWithConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.highlighting.ParadoxSemanticHighlighterColors
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.references.CwtConfigBasedPsiPolyVariantReference
import icu.windea.pls.lang.resolve.ParadoxExpressionService

class ParadoxCommandFieldPrefixNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val linkConfigs: List<CwtLinkConfig>
) : ParadoxComplexExpressionNodeBase(), ParadoxLinkPrefixNode {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return linkConfigs
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxSemanticHighlighterColors.commandFieldPrefix(element.language)
    }

    override fun getReference(element: ParadoxExpressionElement): Reference {
        linkConfigs.forEachFast { it.resolveElementWithConfig() }
        val offset = ParadoxExpressionService.getExpressionOffset(element)
        return Reference(element, rangeInExpression.shiftRight(offset), linkConfigs)
    }

    class Reference(element: PsiElement, rangeInElement: TextRange, configs: List<CwtLinkConfig>) :
        CwtConfigBasedPsiPolyVariantReference<CwtProperty>(element, rangeInElement, configs)

    companion object {
        @JvmStatic
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, linkConfigs: List<CwtLinkConfig>): ParadoxCommandFieldPrefixNode {
            return ParadoxCommandFieldPrefixNode(text, textRange, configGroup, linkConfigs)
        }
    }
}
