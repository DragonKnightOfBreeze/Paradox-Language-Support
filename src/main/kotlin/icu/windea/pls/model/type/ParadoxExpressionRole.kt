package icu.windea.pls.model.type

import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.expressions.ParadoxExpression

/**
 * @see ParadoxExpressionElement
 * @see ParadoxExpression
 */
enum class ParadoxExpressionRole(val text: String) {
    Key("key"),
    Value("value"),
    Other("(other)"),
    ;

    override fun toString() = text
}
