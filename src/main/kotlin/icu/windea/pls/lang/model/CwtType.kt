package icu.windea.pls.lang.model

enum class CwtType(
    val id: Byte,
    val text: kotlin.String
) {
    Unknown(0, "(unknown)"),
    Boolean(1, "boolean"),
    Int(2, "int"),
    Float(3, "float"),
    String(4, "string"),
    Block(5, "block");
    
    override fun toString(): kotlin.String {
        return text
    }
    
    companion object {
        private val values = values()
        
        @JvmStatic
        fun resolve(id: Byte): CwtType {
            //access array rather than byte-key map to optimize performance
            return values[id.toInt()]
        }
    }
}