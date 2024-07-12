package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.highlighter.*

class ParadoxParameterizedValueFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
) : ParadoxComplexExpressionNode.Base(), ParadoxValueFieldNode {
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return ParadoxScriptAttributesKeys.VALUE_LINK_VALUE_KEY
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange): ParadoxParameterizedValueFieldNode? {
            if(!text.isParameterized()) return null
            return ParadoxParameterizedValueFieldNode(text, textRange)
        }
    }
}
