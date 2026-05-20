package icu.windea.pls.model.type

enum class ParadoxSeparatorType(val text: String) {
    Equal("="),
    NotEqual("!="),
    SafeEqual("?="), // #86 supported in ck3, vic3 and eu5
    Lt("<"),
    Gt(">"),
    Le("<="),
    Ge(">="),
    ;

    override fun toString() = text
}
