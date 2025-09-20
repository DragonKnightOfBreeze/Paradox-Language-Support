package icu.windea.pls.model

enum class ParadoxSeparatorType(val id: String) {
    EQUAL("="),
    NOT_EQUAL("!="),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">="),
    SAFE_EQUAL("?="), //supported in ck3 and vic3, see #102
    ;

    override fun toString() = id

    companion object {
        @JvmStatic
        fun resolve(text: String): ParadoxSeparatorType? {
            return when (text) {
                "=" -> EQUAL
                "!=", "<>" -> NOT_EQUAL
                "<" -> LT
                ">" -> GT
                "<=" -> LE
                ">=" -> GE
                "?=" -> SAFE_EQUAL
                else -> null
            }
        }
    }
}
