@file:Suppress("unused")

package icu.windea.pls.lang.util.builders

object ParadoxScriptTextBuilder {
    fun parameter(name: String) = "$${name}$"
    fun parameter(name: String, defaultValue: String) = "$${name}|${defaultValue}$"
    fun conditionalBlock(expression: String, block: String) = "[[${expression}] ${block} ]"
}
