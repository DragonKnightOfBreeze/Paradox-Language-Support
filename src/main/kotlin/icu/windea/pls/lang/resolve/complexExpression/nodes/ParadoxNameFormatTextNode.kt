package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxNameFormatExpression
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

/**
 * 命名格式表达式（[ParadoxNameFormatExpression]）中的普通文本节点：常量片段（非空白）。
 */
class ParadoxNameFormatTextNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
) : ParadoxComplexExpressionNodeBase() {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return when (element.language) {
            is ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.TEXT_KEY
            else -> ParadoxScriptAttributesKeys.STRING_KEY
        }
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxNameFormatTextNode {
            return ParadoxNameFormatTextNode(text, textRange, configGroup)
        }
    }

    companion object : Resolver()
}
