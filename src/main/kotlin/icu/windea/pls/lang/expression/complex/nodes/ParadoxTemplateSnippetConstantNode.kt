package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.expression.complex.*

/**
 * @see ParadoxTemplateExpression
 */
class ParadoxTemplateSnippetConstantNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode.Base() {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxTemplateSnippetConstantNode {
            //text may contain parameters
            return ParadoxTemplateSnippetConstantNode(text, textRange, configGroup)
        }
    }
}
