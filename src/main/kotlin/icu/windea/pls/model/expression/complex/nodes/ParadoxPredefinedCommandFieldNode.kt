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

class ParadoxPredefinedCommandFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val config: CwtLocalisationCommandConfig
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandFieldNode {
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.PREDEFINED_COMMAND_FIELD_KEY
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionHandler.getExpressionOffset(element))
        return Reference(element, rangeInElement, config.pointer.element)
    }
    
    class Reference(element: PsiElement, rangeInElement: TextRange, resolved: CwtProperty?) :
        PsiResolvedReference<CwtProperty>(element, rangeInElement, resolved)
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxPredefinedCommandFieldNode? {
            val config = configGroup.localisationCommands[text] ?: return null
            return ParadoxPredefinedCommandFieldNode(text, textRange, config)
        }
    }
}

    
