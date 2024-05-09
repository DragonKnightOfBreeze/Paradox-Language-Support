package icu.windea.pls.model

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
        @JvmStatic
        fun resolve(id: Byte): CwtType {
            return entries[id.toInt()]
        }
    }
}