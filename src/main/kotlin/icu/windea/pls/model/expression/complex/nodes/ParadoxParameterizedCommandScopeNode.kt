package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.highlighter.*

class ParadoxParameterizedCommandScopeNode(
    override val text: String,
    override val rangeInExpression: TextRange,
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandScopeNode, ParadoxParameterizedNode {
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.DYNAMIC_COMMAND_SCOPE_KEY
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange): ParadoxParameterizedCommandScopeNode? {
            if(!text.isParameterized()) return null
            return ParadoxParameterizedCommandScopeNode(text, textRange)
        }
    }
}
