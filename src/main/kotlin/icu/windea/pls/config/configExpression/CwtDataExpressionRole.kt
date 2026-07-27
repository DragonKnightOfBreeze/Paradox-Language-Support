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

    @Suppress("unused")
    fun isKey() = this == Key

    @Suppress("unused")
    fun isValue() = this == Value

    @Suppress("unused")
    fun toBoolean(): Boolean? = if (this == Key) true else if (this === Value) false else null

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun fromBoolean(value: Boolean?): CwtDataExpressionRole = if (value == true) Key else if (value == false) Value else Other
    }
}
