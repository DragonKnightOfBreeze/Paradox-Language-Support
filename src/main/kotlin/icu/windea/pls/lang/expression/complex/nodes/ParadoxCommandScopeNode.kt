package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.references.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.highlighter.*

class ParadoxCommandScopeNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val config: CwtLocalisationLinkConfig
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandScopeLinkNode {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_KEY
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        val resolved = config.pointer.element?.bindConfig(config)
        return Reference(element, rangeInElement, resolved)
    }
    
    class Reference(element: PsiElement, rangeInElement: TextRange, resolved: CwtProperty?) :
        PsiResolvedReference<CwtProperty>(element, rangeInElement, resolved)
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandScopeNode? {
            if(text.isParameterized()) return null
            val config = configGroup.localisationLinks[text] ?: return null
            return ParadoxCommandScopeNode(text, textRange, configGroup, config)
        }
    }
}
