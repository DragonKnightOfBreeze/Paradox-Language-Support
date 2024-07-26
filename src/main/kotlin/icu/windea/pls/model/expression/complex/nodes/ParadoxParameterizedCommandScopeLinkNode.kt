package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.highlighter.*

class ParadoxParameterizedCommandScopeLinkNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandScopeLinkNode, ParadoxParameterizedNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxParameterizedCommandScopeLinkNode? {
            if(!text.isParameterized()) return null
            return ParadoxParameterizedCommandScopeLinkNode(text, textRange, configGroup)
        }
    }
}
