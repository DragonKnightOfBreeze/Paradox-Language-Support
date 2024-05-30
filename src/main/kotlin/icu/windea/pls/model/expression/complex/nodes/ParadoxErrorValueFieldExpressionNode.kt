package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.expression.complex.errors.*
import icu.windea.pls.script.psi.*

class ParadoxErrorValueFieldExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange
) : ParadoxValueFieldExpressionNode, ParadoxErrorExpressionNode {
    override fun getUnresolvedError(element: ParadoxScriptStringExpressionElement): ParadoxExpressionError? {
        if(nodes.isNotEmpty()) return null
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        return ParadoxUnresolvedScopeExpressionError(rangeInExpression, PlsBundle.message("script.expression.unresolvedValueField", text))
    }
}
