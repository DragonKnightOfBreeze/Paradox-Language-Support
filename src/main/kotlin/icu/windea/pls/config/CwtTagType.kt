package icu.windea.pls.config

enum class CwtTagType(
    val id: String
) {
    Predefined("tag"), // ## tag
    TypeKeyPrefix("type key prefix"), // type_key_prefix = xxx
    ;
}
