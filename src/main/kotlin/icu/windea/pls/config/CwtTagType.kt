package icu.windea.pls.config

enum class CwtTagType(
    val id: String
) {
    Predefined("predefined"), // ## tag
    TypeKeyPrefix("type key prefix"), // type_key_prefix = xxx
    ;
}
