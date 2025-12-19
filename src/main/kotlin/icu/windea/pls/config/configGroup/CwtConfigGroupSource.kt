package icu.windea.pls.config.configGroup

/**
 * 规则分组来源。
 */
enum class CwtConfigGroupSource(val id: String) {
    BuiltIn("builtin"),
    Remote("remote"),
    Local("local"),
    ;

    override fun toString(): String = id
}

