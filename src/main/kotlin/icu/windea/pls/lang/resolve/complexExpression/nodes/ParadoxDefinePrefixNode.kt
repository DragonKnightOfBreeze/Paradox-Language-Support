package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.editor.ParadoxSemanticAttributesKeys
import icu.windea.pls.lang.psi.ParadoxExpressionElement

class ParadoxDefinePrefixNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNodeBase() {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxSemanticAttributesKeys.keyword()
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDefinePrefixNode {
            return ParadoxDefinePrefixNode(text, textRange, configGroup)
        }
    }

    companion object : Resolver()
}
