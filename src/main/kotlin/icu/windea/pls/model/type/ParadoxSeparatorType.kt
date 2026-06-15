package icu.windea.pls.model.type

enum class ParadoxSeparatorType(val text: String) {
    Equal("="),
    NotEqual("!="),
    // #86 supported in ck3, vic3 and eu5
    // 2.1.10 supported in stellaris 4.4, with possible blank between `?` and `=` (is this really valid and suitable???)
    SafeEqual("?="),
    Lt("<"),
    Gt(">"),
    Le("<="),
    Ge(">="),
    ;

    override fun toString() = text
}
