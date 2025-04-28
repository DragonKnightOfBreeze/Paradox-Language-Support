package icu.windea.pls.model

enum class ParadoxType(
    val text: String
) {
    Unknown("(unknown)"),
    Boolean("boolean"),
    Int("int"),
    Float("float"),
    String("string"),
    Color("color"),
    Block("block"),
    Parameter("parameter"),
    InlineMath("inline math"),
    CommandExpression("command expression"),
    DatabaseObjectExpression("database object expression"),
    ;

    override fun toString(): String {
        return text
    }

    fun isBooleanType(): Boolean {
        return this == Boolean
    }

    fun isIntType(): Boolean {
        return this == Unknown || this == Int || this == Parameter || this == InlineMath
    }

    fun isFloatType(): Boolean {
        return this == Unknown || this == Int || this == Float || this == Parameter || this == InlineMath
    }

    fun isNumberType(): Boolean {
        return this == Unknown || this == Int || this == Float || this == Parameter || this == InlineMath
    }

    fun isStringType(): Boolean {
        return this == Unknown || this == String || this == Parameter
    }

    fun isColorType(): Boolean {
        return this == Color
    }

    fun isStringLikeType(): Boolean {
        return this == Unknown || this == String || this == Parameter || this == Int || this == Float
    }

    fun isBlockLikeType(): Boolean {
        return this == Block || this == Color || this == InlineMath
    }

    fun canBeScriptedVariableValue(): Boolean {
        return this == Boolean || this == Int || this == Float || this == String
    }
}
