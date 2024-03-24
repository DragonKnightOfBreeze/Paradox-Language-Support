package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.highlighter.*

class ParadoxParameterizedScopeFieldExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange,
) : ParadoxScopeFieldExpressionNode {
    override fun getAttributesKey() = ParadoxScriptAttributesKeys.SCOPE_KEY
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange): ParadoxParameterizedScopeFieldExpressionNode? {
            if(!text.isParameterized()) return null
            return ParadoxParameterizedScopeFieldExpressionNode(text, textRange)
        }
    }
}
