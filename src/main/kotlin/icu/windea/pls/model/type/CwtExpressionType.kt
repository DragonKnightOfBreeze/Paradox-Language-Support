package icu.windea.pls.model.type

import icu.windea.pls.cwt.psi.CwtExpressionElement

enum class CwtExpressionType(val id: String) {
    Unknown("(unknown)"),
    Boolean("boolean"),
    Int("int"),
    Float("float"),
    String("string"),
    Block("block"),
    ;

    override fun toString() = id
}
