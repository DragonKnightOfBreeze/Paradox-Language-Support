package icu.windea.pls.model

enum class ParadoxType(val id: String) {
    Unknown("(unknown)"),
    Boolean("boolean"),
    Int("int"),
    Float("float"),
    String("string"),
    Color("color"),
    Block("block"),
    Parameter("parameter"),
    InlineMath("inline_math"),
    CommandExpression("command_expression"),
    DatabaseObjectExpression("database_object_expression"),
    ;

    override fun toString() = id
}
