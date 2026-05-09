package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.resolveElementWithConfig
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.editor.ParadoxSemanticAttributesKeys
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.references.CwtConfigBasedPsiPolyVariantReference
import icu.windea.pls.lang.util.ParadoxExpressionManager

class ParadoxCommandScopePrefixNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val linkConfigs: List<CwtLinkConfig>
) : ParadoxComplexExpressionNodeBase(), ParadoxLinkPrefixNode {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return linkConfigs
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxSemanticAttributesKeys.commandScopePrefix(element.language)
    }

    override fun getReference(element: ParadoxExpressionElement): Reference {
        linkConfigs.forEach { it.resolveElementWithConfig() }
        val offset = ParadoxExpressionManager.getExpressionOffset(element)
        return Reference(element, rangeInExpression.shiftRight(offset), linkConfigs)
    }

    class Reference(element: PsiElement, rangeInElement: TextRange, configs: List<CwtLinkConfig>) :
        CwtConfigBasedPsiPolyVariantReference<CwtProperty>(element, rangeInElement, configs)

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, linkConfigs: List<CwtLinkConfig>): ParadoxCommandScopePrefixNode {
            return ParadoxCommandScopePrefixNode(text, textRange, configGroup, linkConfigs)
        }
    }

    companion object : Resolver()
}
