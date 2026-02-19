package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.resolveElementWithConfig
import icu.windea.pls.core.util.values.singletonSet
import icu.windea.pls.core.util.values.to
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.references.CwtConfigBasedPsiReference
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys

class ParadoxSystemCommandScopeNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val config: CwtSystemScopeConfig
) : ParadoxComplexExpressionNodeBase(), ParadoxCommandScopeLinkNode, ParadoxSystemScopeAwareLinkNode, ParadoxIdentifierNode {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return config.to.singletonSet()
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.SYSTEM_COMMAND_SCOPE_KEY
    }

    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        config.resolveElementWithConfig()
        return Reference(element, rangeInElement, config)
    }

    class Reference(
        element: PsiElement,
        rangeInElement: TextRange,
        config: CwtSystemScopeConfig
    ) : CwtConfigBasedPsiReference<CwtProperty>(element, rangeInElement, config), ParadoxIdentifierNode.Reference

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxSystemCommandScopeNode? {
            if (text.isParameterized()) return null
            val config = configGroup.systemScopes[text] ?: return null
            return ParadoxSystemCommandScopeNode(text, textRange, configGroup, config)
        }
    }

    companion object : Resolver()
}
