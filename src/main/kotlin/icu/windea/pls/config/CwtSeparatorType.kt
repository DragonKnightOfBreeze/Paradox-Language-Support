package icu.windea.pls.config

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
        private val values = values()
        
        @JvmStatic
        fun resolve(id: Byte): CwtSeparatorType {
            //access array rather than byte-key map to optimize performance
            return values[id.toInt()]
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