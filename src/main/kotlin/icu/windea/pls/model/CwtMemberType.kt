package icu.windea.pls.model

enum class CwtMemberType(val id: String) {
    PROPERTY("property"),
    VALUE("value"),
    ;

    override fun toString() = id
}
