package icu.windea.pls.model

import icu.windea.pls.model.CwtType

enum class CwtType(val id: String) {
    Unknown("(unknown)"),
    Boolean("boolean"),
    Int("int"),
    Float("float"),
    String("string"),
    Block("block"),
    ;

    override fun toString() = id
}
