package icu.windea.pls.core.expression.errors

import com.intellij.openapi.util.*

class ParadoxMissingVariableExpressionError(
    override val rangeInExpression: TextRange,
    override val description: String
) : ParadoxMissingExpressionError