package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.highlighter.*

class ParadoxParameterizedValueFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
) : ParadoxValueFieldNode {
    override fun getAttributesKey() = ParadoxScriptAttributesKeys.VALUE_LINK_VALUE_KEY
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange): ParadoxParameterizedValueFieldNode? {
            if(!text.isParameterized()) return null
            return ParadoxParameterizedValueFieldNode(text, textRange)
        }
    }
}
