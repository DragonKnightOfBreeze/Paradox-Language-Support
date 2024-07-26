package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.highlighter.*
import icu.windea.pls.script.highlighter.*

class ParadoxMarkerNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode.Base(), ParadoxTokenNode {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return when(element.language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.MARKER_KEY
            else -> ParadoxScriptAttributesKeys.MARKER_KEY
        }
    }
}
