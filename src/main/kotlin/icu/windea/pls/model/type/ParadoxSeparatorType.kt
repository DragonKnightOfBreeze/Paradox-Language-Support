package icu.windea.pls.model.type

enum class ParadoxSeparatorType(val text: String) {
    Equal("="),
    NotEqual("!="),
    Lt("<"),
    Gt(">"),
    Le("<="),
    Ge(">="),

    // #86 supported in ck3, vic3, eu5 (preferred format: `k ?= v`)
    SafeAssign("? ="),
    // 2.1.10 #331 supported in stellaris 4.4 (preferred format: `k? = v`)
    SafeCallAssign("?="),
    ;

    override fun toString() = text

    // region Inline Methods

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun optimized(): Byte = ordinal.toByte() // 3.0.1 radical optimization

    // endregion

    companion object {
        // region Inline Methods

        @Suppress("NOTHING_TO_INLINE", "unused")
        inline fun deoptimized(value: Byte): ParadoxSeparatorType = entries[value.toInt()] // 3.0.1 radical optimization

        // endregion
    }
}
