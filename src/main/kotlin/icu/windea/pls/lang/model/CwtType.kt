package icu.windea.pls.lang.model

enum class CwtType(
    val id: kotlin.String,
    val text: kotlin.String
) {
    Unknown("unknown", "(unknown)"),
    Boolean("boolean", "boolean"),
    Int("int", "int"),
    Float("float", "float"),
    String("string", "string"),
    Block("block", "block")
}