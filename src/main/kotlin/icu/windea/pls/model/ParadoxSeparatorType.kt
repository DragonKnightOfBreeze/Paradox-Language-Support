package icu.windea.pls.model;

import icu.windea.pls.core.annotations.*

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
    @WithGameType(ParadoxGameType.Vic3)
    COMPARE("?=");
    
    override fun toString(): String {
        return text
    }
}