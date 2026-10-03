package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression

/**
 * @see ParadoxTemplateExpression
 */
class ParadoxTemplateSnippetConstantNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val constant: String,
) : ParadoxComplexExpressionNodeBase() {
    fun getMockConfig(): CwtValueConfig {
        return CwtValueConfig.mock(configGroup, constant)
    }

    companion object {
        @JvmStatic
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, constant: String): ParadoxTemplateSnippetConstantNode {
            // text may contain parameters
            return ParadoxTemplateSnippetConstantNode(text, textRange, configGroup, constant)
        }
    }
}
