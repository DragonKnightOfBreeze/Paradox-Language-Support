package icu.windea.pls.lang.resolving.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.bindConfig
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.psi.PsiResolvedPolyVariantReference
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

class ParadoxScopeLinkPrefixNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val linkConfigs: List<CwtLinkConfig>
) : ParadoxComplexExpressionNodeBase(), ParadoxLinkPrefixNode {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return linkConfigs
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxScriptAttributesKeys.SCOPE_LINK_PREFIX_KEY
    }

    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        val resolved = linkConfigs.mapNotNull { it.pointer.element?.bindConfig(it) }
        return Reference(element, rangeInElement, resolved)
    }

    class Reference(element: ParadoxExpressionElement, rangeInElement: TextRange, resolved: List<CwtProperty>) :
        PsiResolvedPolyVariantReference<CwtProperty>(element, rangeInElement, resolved)

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, linkConfigs: List<CwtLinkConfig>): ParadoxScopeLinkPrefixNode {
            return ParadoxScopeLinkPrefixNode(text, textRange, configGroup, linkConfigs)
        }
    }

    companion object : Resolver()
}
