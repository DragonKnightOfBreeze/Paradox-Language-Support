package icu.windea.pls.model.type

import icu.windea.pls.cwt.psi.CwtExpressionElement

/**
 * @see CwtExpressionElement
 */
enum class CwtExpressionRole(val text: String) {
    Key("key"),
    Value("value"),
    Other("(other)"),
    ;

    override fun toString() = text
}
