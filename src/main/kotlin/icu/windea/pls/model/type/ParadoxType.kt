package icu.windea.pls.model.type

enum class ParadoxType(val id: String) {
    Unknown("(unknown)"),
    Boolean("boolean"),
    Int("int"),
    Float("float"),
    String("string"),
    Block("block"),
    Color("color"),
    InlineMath("inline_math"),
    ScriptedVariableReference("scripted_variable_reference"),
    LocalisationProperty("localisation_property"),
    Parameter("parameter"),
    ConditionParameter("condition_parameter"),
    LocalisationParameter("localisation_parameter"),
    ;

    override fun toString() = id

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
