package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.script.highlighter.*

class ParadoxMarkerNode(
    override val text: String,
    override val rangeInExpression: TextRange
) : ParadoxTokenNode {
    override fun getAttributesKey() = ParadoxScriptAttributesKeys.MARKER_KEY
}
