package icu.windea.pls.model

enum class ParadoxType(
    val text: kotlin.String
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
    
    override fun toString(): kotlin.String {
        return text
    }
    
    fun isBooleanType(): kotlin.Boolean {
        return this == Boolean
    }
    
    fun isIntType(): kotlin.Boolean {
        return this == Unknown || this == Int || this == Parameter || this == InlineMath
    }
    
    fun isFloatType(): kotlin.Boolean {
        return this == Unknown || this == Int || this == Float || this == Parameter || this == InlineMath
    }
    
    fun isNumberType(): kotlin.Boolean {
        return this == Unknown || this == Int || this == Float || this == Parameter || this == InlineMath
    }
    
    fun isStringType(): kotlin.Boolean {
        return this == Unknown || this == String || this == Parameter
    }
    
    fun isColorType(): kotlin.Boolean {
        return this == Color
    }
    
    fun isStringLikeType(): kotlin.Boolean {
        return this == Unknown || this == String || this == Parameter || this == Int || this == Float
    }
    
    fun isBlockLikeType(): kotlin.Boolean {
        return this == Block || this == Color || this == InlineMath
    }
    
    fun canBeScriptedVariableValue(): kotlin.Boolean {
        return this == Boolean || this == Int || this == Float || this == String
    }
}
