package icu.windea.pls.model

enum class ParadoxSeparatorType(
    val text: String
) {
    EQUAL("="),
    NOT_EQUAL("!="),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">="),
    QUESTION_EQUAL("?="), //same as "=", used in several games, see #86
    ;
    
    override fun toString(): String {
        return text
    }
    
    companion object {
        @JvmStatic
        fun resolve(text: String): ParadoxSeparatorType? {
            return when(text) {
                "=" -> EQUAL
                "!=", "<>" -> NOT_EQUAL
                "<" -> LT
                ">" -> GT
                "<=" -> LE
                ">=" -> GE
                "?=" -> QUESTION_EQUAL
                else -> null
            }
        }
    }
}
