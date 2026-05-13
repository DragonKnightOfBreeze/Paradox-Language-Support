package icu.windea.pls.model.type

enum class ParadoxSeparatorType(val text: String) {
    EQUAL("="),
    NOT_EQUAL("!="),
    SAFE_EQUAL("?="), // #86 supported in ck3, vic3 and eu5
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">="),
    ;

    override fun toString() = text
}
