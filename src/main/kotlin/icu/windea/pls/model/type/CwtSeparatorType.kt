package icu.windea.pls.model.type

enum class CwtSeparatorType(val text: String) {
    Equal("="), // logic equal
    NotEqual("!="), // logic not equal
    DoubleEqual("=="), // matches comparison operators
    ;

    override fun toString() = text
}
