package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.editor.*
import icu.windea.pls.script.editor.*

class ParadoxMarkerNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode.Base(), ParadoxTokenNode {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return when (element.language) {
            is ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.MARKER_KEY
            else -> ParadoxScriptAttributesKeys.MARKER_KEY
        }
    }
}
