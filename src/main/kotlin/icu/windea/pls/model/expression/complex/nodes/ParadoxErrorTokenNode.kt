package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*

class ParadoxErrorTokenNode(
    override val text: String,
    override val rangeInExpression: TextRange
) : ParadoxErrorNode
