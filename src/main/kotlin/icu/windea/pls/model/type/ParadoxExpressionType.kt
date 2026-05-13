package icu.windea.pls.model.type

enum class ParadoxExpressionType(val text: String) {
    Unknown("(unknown)"),
    Boolean("boolean"),
    Int("int"),
    Float("float"),
    String("string"),
    Block("block"),
    Color("color"),
    InlineMath("inline math"),
    CommandExpression("command expression"),
    DatabaseObjectExpression("database object expression"),
    ;

    override fun toString() = text

    // region Matchers

    fun isLenientInt(): Boolean {
        return this == Int || this == InlineMath || this == Unknown
    }

    fun isLenientFloat(): Boolean {
        return this == Int || this == Float || this == InlineMath || this == Unknown
    }

    fun isLenientString(): Boolean {
        return this == String || this == Unknown
    }

    fun isNumberOrLenientString(): Boolean {
        return this == Int || this == Float || this == String || this == Unknown
    }

    fun isBlockLike(): Boolean {
        return this == Block || this == Color || this == InlineMath
    }

    // endregion
}
