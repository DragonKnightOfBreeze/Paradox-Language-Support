package icu.windea.pls.model

enum class CwtSeparatorType(
    val id: Byte,
    val text: String
) {
    EQUAL(0, "="),
    NOT_EQUAL(1, "!=");
    
    override fun toString(): String {
        return text
    }
    
    companion object {
        @JvmStatic
        fun resolve(id: Byte): CwtSeparatorType {
            return entries[id.toInt()]
        }
        
        @JvmStatic
        fun resolve(text: String): CwtSeparatorType? {
            return when(text) {
                "=", "==" -> EQUAL
                "<>", "!=" -> NOT_EQUAL
                else -> null
            }
        }
    }
}