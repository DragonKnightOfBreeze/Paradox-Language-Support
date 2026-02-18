package icu.windea.pls.lang.util.builders

@Suppress("unused")
object ParadoxScriptTextBuilder {
    fun parameter(name: String) = "$${name}$"
    fun parameter(name: String, defaultValue: String) = "$${name}|${defaultValue}$"
    fun parameterCondition(expression: String, block: String) = "[[${expression}] ${block} ]"
}
