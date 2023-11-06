package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.highlighter.*

class ParadoxParameterizedValueFieldExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange,
) : ParadoxValueFieldExpressionNode {
    override fun getAttributesKey() = ParadoxScriptAttributesKeys.VALUE_LINK_VALUE_KEY
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange): ParadoxParameterizedValueFieldExpressionNode? {
            if(!text.isParameterized()) return null
            return ParadoxParameterizedValueFieldExpressionNode(text, textRange)
        }
    }
}
