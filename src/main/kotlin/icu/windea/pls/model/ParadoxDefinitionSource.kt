package icu.windea.pls.model

/**
 * 定义的来源。
 */
enum class ParadoxDefinitionSource {
    File,
    Property,
    Inline,
    Injection,
    ;

    // region Inline Methods

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun optimized(): Byte = ordinal.toByte() // 3.0.1 radical optimization

    // endregion

    companion object {
        // region Inline Methods

        @Suppress("NOTHING_TO_INLINE", "unused")
        inline fun deoptimized(value: Byte): ParadoxDefinitionSource = entries[value.toInt()] // 3.0.1 radical optimization

        // endregion
    }
}
