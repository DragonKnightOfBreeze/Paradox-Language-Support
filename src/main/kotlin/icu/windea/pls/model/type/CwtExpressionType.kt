package icu.windea.pls.model.type

import icu.windea.pls.cwt.psi.CwtExpressionElement

/**
 * @see CwtExpressionElement
 */
enum class CwtExpressionType(val text: String) {
    Unknown("(unknown)"),
    Boolean("boolean"),
    Int("int"),
    Float("float"),
    String("string"),
    Block("block"),
    ;

    override fun toString() = text
}
