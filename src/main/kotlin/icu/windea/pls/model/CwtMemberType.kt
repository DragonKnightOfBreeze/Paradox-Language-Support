package icu.windea.pls.model

enum class CwtMemberType(val id: String) {
    NONE("none"),
    MIXED("mixed"),
    PROPERTY("property"),
    VALUE("value"),
    ;

    override fun toString() = id
}
