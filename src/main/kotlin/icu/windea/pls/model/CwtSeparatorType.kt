package icu.windea.pls.model

enum class CwtSeparatorType(
    val text: String
) {
    EQUAL("="),
    NOT_EQUAL("!="),
    ;

    override fun toString(): String {
        return text
    }

    companion object {
        @JvmStatic
        fun resolve(text: String): CwtSeparatorType? {
            return when (text) {
                "=", "==" -> EQUAL
                "<>", "!=" -> NOT_EQUAL
                else -> null
            }
        }
    }
}
