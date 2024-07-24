package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
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
import icu.windea.pls.script.highlighter.*

class ParadoxSystemCommandScopeNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val config: CwtSystemLinkConfig
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandScopeNode {
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.SYSTEM_COMMAND_SCOPE_KEY
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionHandler.getExpressionOffset(element))
        return Reference(element, rangeInElement, config.pointer.element)
    }
    
    class Reference(element: PsiElement, rangeInElement: TextRange, resolved: CwtProperty?) :
        PsiResolvedReference<CwtProperty>(element, rangeInElement, resolved)
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxSystemCommandScopeNode? {
            val config = configGroup.systemLinks[text] ?: return null
            return ParadoxSystemCommandScopeNode(text, textRange, config)
        }
    }
}
