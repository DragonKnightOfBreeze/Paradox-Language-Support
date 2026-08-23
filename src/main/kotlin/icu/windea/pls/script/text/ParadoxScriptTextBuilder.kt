@file:Suppress("unused")

package icu.windea.pls.script.text

object ParadoxScriptTextBuilder {
    fun inlineMath(expression: String) = "@[ $expression ]"
    fun parameter(name: String) = "$${name}$"
    fun parameter(name: String, defaultValue: String) = "$${name}|${defaultValue}$"
    fun conditionalBlock(expression: String, block: String) = "[[${expression}] ${block} ]"
}
