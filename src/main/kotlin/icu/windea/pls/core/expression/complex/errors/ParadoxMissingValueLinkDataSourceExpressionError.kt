package icu.windea.pls.core.expression.complex.errors

import com.intellij.openapi.util.*

class ParadoxMissingValueLinkDataSourceExpressionError(
    override val rangeInExpression: TextRange,
    override val description: String
) : ParadoxMissingExpressionError
