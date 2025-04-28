package icu.windea.pls.model

enum class CwtType(
    val text: String
) {
    Unknown("(unknown)"),
    Boolean("boolean"),
    Int("int"),
    Float("float"),
    String("string"),
    Block("block"),
    ;

    override fun toString(): String {
        return text
    }
}
