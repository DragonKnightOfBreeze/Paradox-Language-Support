package icu.windea.pls.model

enum class CwtType(
    val text: kotlin.String
) {
    Unknown("(unknown)"),
    Boolean("boolean"),
    Int("int"),
    Float("float"),
    String("string"),
    Block("block"),
    ;
    
    override fun toString(): kotlin.String {
        return text
    }
}
