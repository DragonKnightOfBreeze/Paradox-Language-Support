package icu.windea.pls.model

enum class CwtSeparatorType(val id: String) {
    EQUAL("="),
    NOT_EQUAL("!="),
    ;

    override fun toString() = id

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
