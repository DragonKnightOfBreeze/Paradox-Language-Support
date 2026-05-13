package icu.windea.pls.model.type

enum class ParadoxExpressionType(val id: String) {
    Unknown("(unknown)"),
    Boolean("boolean"),
    Int("int"),
    Float("float"),
    String("string"),
    Block("block"),
    Color("color"),
    InlineMath("inline_math"),
    CommandExpression("command expression"),
    DatabaseObjectExpression("database object expression"),
    ;

    override fun toString() = id

    // region Matchers

    fun isRelaxInt(): Boolean {
        return this == Int || this == InlineMath || this == Unknown
    }

    fun isRelaxFloat(): Boolean {
        return this == Int || this == Float || this == InlineMath || this == Unknown
    }

    fun isRelaxString(): Boolean {
        return this == String || this == Unknown
    }

    fun isNumberOrRelaxString(): Boolean {
        return this == Int || this == Float || this == String || this == Unknown
    }

    fun isBlockLike(): Boolean {
        return this == Block || this == Color || this == InlineMath
    }

    // endregion
}
