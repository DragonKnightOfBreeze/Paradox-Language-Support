package icu.windea.pls.model.type

enum class ParadoxType(val text: String) {
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

    override fun toString() = text
}
