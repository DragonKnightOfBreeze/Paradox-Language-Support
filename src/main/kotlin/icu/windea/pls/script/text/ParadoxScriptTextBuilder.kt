@file:Suppress("unused")

package icu.windea.pls.script.text

/**
 * 这个构建器提供了一组常见的构建方法，从而支持以字符串插值的格式，或者说类 DSL 的格式，构建脚本文本。
 *
 * 并且，可以用来规避讨厌的 $。
 */
object ParadoxScriptTextBuilder {
    fun inlineMath(expression: String) = "@[ $expression ]"
    fun parameter(name: String) = "$${name}$"
    fun parameter(name: String, defaultValue: String) = "$${name}|${defaultValue}$"
    fun conditionalBlock(expression: String, block: String) = "[[${expression}] ${block} ]"
}
