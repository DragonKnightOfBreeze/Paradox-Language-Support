package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.highlighter.*

class ParadoxDynamicCommandScopeNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandScopeNode {
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return ParadoxScriptAttributesKeys.DYNAMIC_VALUE_KEY
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionHandler.getExpressionOffset(element))
        return Reference(element, rangeInElement, text, configGroup)
    }
    
    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        val name: String,
        val configGroup: CwtConfigGroup
    ) : PsiReferenceBase<ParadoxExpressionElement>(element, rangeInElement), PsiReferencesAware {
        val configExpressions = listOf(
            configGroup.mockEventTargetConfig.expression,
            configGroup.mockGlobalEventTargetConfig.expression,
        )
        
        override fun handleElementRename(newElementName: String): PsiElement {
            return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
        }
        
        override fun resolve(): PsiElement? {
            return ParadoxDynamicValueHandler.resolveDynamicValue(element, name, configExpressions, configGroup)
        }
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDynamicCommandScopeNode {
            return ParadoxDynamicCommandScopeNode(text, textRange, configGroup)
        }
    }
}
