package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.highlighter.*

class ParadoxParameterizedCommandFieldLinkNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandFieldLinkNode, ParadoxParameterizedNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxParameterizedCommandFieldLinkNode? {
            if(!text.isParameterized()) return null
            return ParadoxParameterizedCommandFieldLinkNode(text, textRange, configGroup)
        }
    }
}
