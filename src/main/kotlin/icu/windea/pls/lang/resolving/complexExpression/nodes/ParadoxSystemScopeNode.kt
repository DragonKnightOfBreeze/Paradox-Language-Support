package icu.windea.pls.lang.resolving.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.bindConfig
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.systemScopes
import icu.windea.pls.core.psi.PsiResolvedReference
import icu.windea.pls.core.util.set
import icu.windea.pls.core.util.singleton
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

class ParadoxSystemScopeNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val config: CwtSystemScopeConfig
) : ParadoxComplexExpressionNodeBase(), ParadoxScopeLinkNode {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return config.singleton.set()
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxScriptAttributesKeys.SYSTEM_SCOPE_KEY
    }

    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        val resolved = config.pointer.element?.bindConfig(config)
        return Reference(element, rangeInElement, resolved)
    }

    class Reference(element: PsiElement, rangeInElement: TextRange, resolved: CwtProperty?) :
        PsiResolvedReference<CwtProperty>(element, rangeInElement, resolved)

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxSystemScopeNode? {
            if (text.isParameterized()) return null
            val config = configGroup.systemScopes.get(text) ?: return null
            return ParadoxSystemScopeNode(text, textRange, configGroup, config)
        }
    }

    companion object : Resolver()
}
