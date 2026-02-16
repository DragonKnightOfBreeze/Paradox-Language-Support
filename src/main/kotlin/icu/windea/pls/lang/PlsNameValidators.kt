package icu.windea.pls.lang

object PlsNameValidators {
    // NOTE 2.1.3 部分 `RenameInputValidator` 是有必要实现的，因为对应的 `name` 也接受额外字符

    fun checkScriptedVariableName(name: String): Boolean = name.isNotEmpty() && name.isIdentifier()
    fun checkScriptPropertyName(name: String): Boolean = name.isNotEmpty() && name.isIdentifier(".-")
    fun checkLocalisationPropertyName(name: String): Boolean = name.isNotEmpty() && name.isIdentifier(".-")
    fun checkDefinitionName(name: String): Boolean = name.isNotEmpty() && name.isIdentifier(".-")
    fun checkLocalisationName(name: String): Boolean = name.isNotEmpty() && name.isIdentifier(".-'")
    fun checkParameterName(name: String): Boolean = name.isNotEmpty() && name.isIdentifier()
    fun checkLocalisationParameterName(name: String): Boolean = name.isNotEmpty() && name.isIdentifier(".-'")
}
