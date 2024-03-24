package icu.windea.pls.model.expression.complex.errors

import com.intellij.openapi.util.*

class ParadoxUnresolvedScopeExpressionError(
    override val rangeInExpression: TextRange,
    override val description: String
) : ParadoxUnresolvedExpressionError


