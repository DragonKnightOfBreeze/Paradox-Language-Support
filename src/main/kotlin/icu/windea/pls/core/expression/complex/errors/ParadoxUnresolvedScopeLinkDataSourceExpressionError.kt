package icu.windea.pls.core.expression.complex.errors

import com.intellij.openapi.util.*

class ParadoxUnresolvedScopeLinkDataSourceExpressionError(
    override val rangeInExpression: TextRange,
    override val description: String
) : ParadoxUnresolvedExpressionError

