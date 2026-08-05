package icu.windea.pls.config.configExpression

/**
 * @see CwtDataExpression
 */
enum class CwtDataExpressionRole(val text: String) {
    Key("key"),
    Value("value"),
    Other("(other)")
    ;

    override fun toString() = text

    // region Inline Methods

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun toBoolean(): Boolean? = if (this == Key) true else if (this === Value) false else null

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun isKey(): Boolean = this == Key

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun isValue(): Boolean = this == Value

    // endregion

    companion object {
        // region Inline Methods

        @Suppress("NOTHING_TO_INLINE", "unused")
        fun fromBoolean(value: Boolean?): CwtDataExpressionRole = if (value == true) Key else if (value == false) Value else Other

        // endregion
    }
}
