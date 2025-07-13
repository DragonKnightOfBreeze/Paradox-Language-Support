package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.editor.*

class ParadoxSystemCommandScopeNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val config: CwtSystemScopeConfig
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandScopeLinkNode {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return config.toSingletonSet()
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.SYSTEM_COMMAND_SCOPE_KEY
    }

    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        val resolved = config.pointer.element?.bindConfig(config)
        return Reference(element, rangeInElement, resolved)
    }

    class Reference(element: PsiElement, rangeInElement: TextRange, resolved: CwtProperty?) :
        PsiResolvedReference<CwtProperty>(element, rangeInElement, resolved)

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxSystemCommandScopeNode? {
            if (text.isParameterized()) return null
            val config = configGroup.systemScopes[text] ?: return null
            return ParadoxSystemCommandScopeNode(text, textRange, configGroup, config)
        }
    }
}
