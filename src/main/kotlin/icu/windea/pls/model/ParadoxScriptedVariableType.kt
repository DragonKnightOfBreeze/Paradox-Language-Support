package icu.windea.pls.model

enum class ParadoxScriptedVariableType(val id: String) {
    Local("local"),
    Global("global"),
    ;

    override fun toString() = id
}
