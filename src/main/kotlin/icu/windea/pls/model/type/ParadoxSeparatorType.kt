package icu.windea.pls.model.type

enum class ParadoxSeparatorType(val text: String) {
    Equal("="),
    NotEqual("!="),
    Lt("<"),
    Gt(">"),
    Le("<="),
    Ge(">="),

    // #86 supported in ck3, vic3 and eu5 (preferred format: `k ?= v`)
    SafeAssign("? ="),
    // NOTE 2.1.10 supported in stellaris 4.4 (preferred format: `k? = v`)
    SafeCallAssign("?="),
    ;

    override fun toString() = text
}
