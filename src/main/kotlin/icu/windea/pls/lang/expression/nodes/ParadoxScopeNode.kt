package icu.windea.pls.lang.expression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.bindConfig
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtLinkConfig
import icu.windea.pls.config.config.forScope
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.links
import icu.windea.pls.core.psi.PsiResolvedReference
import icu.windea.pls.core.util.set
import icu.windea.pls.core.util.singleton
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

class ParadoxScopeNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val config: CwtLinkConfig
) : ParadoxComplexExpressionNode.Base(), ParadoxScopeLinkNode {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return config.singleton.set()
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxScriptAttributesKeys.SCOPE_KEY
    }

    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        val resolved = config.pointer.element?.bindConfig(config)
        return Reference(element, rangeInElement, resolved)
    }

    class Reference(element: PsiElement, rangeInElement: TextRange, resolved: CwtProperty?) :
        PsiResolvedReference<CwtProperty>(element, rangeInElement, resolved)

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeNode? {
            if (text.isParameterized()) return null
            val config = configGroup.links.get(text)?.takeIf { it.forScope() && !it.fromData } ?: return null
            return ParadoxScopeNode(text, textRange, configGroup, config)
        }
    }
}
