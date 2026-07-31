package icu.windea.pls.model.type

enum class CwtSeparatorType(val text: String) {
    Equal("="), // logic equal
    NotEqual("!="), // logic not equal
    DoubleEqual("=="), // matches comparison operators
    ;

    override fun toString() = text

    // region Inline Methods

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun optimized(): Byte = ordinal.toByte() // 3.0.1 radical optimization

    // endregion

    companion object {
        // region Inline Methods

        @Suppress("NOTHING_TO_INLINE", "unused")
        inline fun deoptimized(value: Byte): CwtSeparatorType = entries[value.toInt()] // 3.0.1 radical optimization

        // endregion
    }
}
