package icu.windea.pls.model;

enum class ParadoxSeparatorType(
    val text: String
) {
    EQUAL("="),
    NOT_EQUAL("!="),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">="),
    //https://github.com/cwtools/cwtools/issues/53
    //can be used in some game types
    COMPARE("?=");
    
    override fun toString(): String {
        return text
    }
}