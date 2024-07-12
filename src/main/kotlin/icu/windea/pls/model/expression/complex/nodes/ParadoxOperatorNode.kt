package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.highlighter.*
import icu.windea.pls.script.highlighter.*

class ParadoxOperatorNode(
    override val text: String,
    override val rangeInExpression: TextRange
) : ParadoxComplexExpressionNode.Base(), ParadoxTokenNode {
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return when(language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.OPERATOR_KEY
            else -> ParadoxScriptAttributesKeys.OPERATOR_KEY
        }
    }
}
