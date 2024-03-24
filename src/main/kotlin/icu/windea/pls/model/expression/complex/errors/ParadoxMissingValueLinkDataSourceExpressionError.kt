package icu.windea.pls.model.expression.complex.errors

import com.intellij.openapi.util.*

class ParadoxMissingValueLinkDataSourceExpressionError(
    override val rangeInExpression: TextRange,
    override val description: String
) : ParadoxMissingExpressionError
