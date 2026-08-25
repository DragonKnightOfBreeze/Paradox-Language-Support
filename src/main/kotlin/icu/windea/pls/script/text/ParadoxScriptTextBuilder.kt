@file:Suppress("unused")

package icu.windea.pls.script.text

/**
 * 这个 DSL 提供了一组常见的构建方法，从而支持以字符串插值的风格，或者说类 DSL 风格，构建脚本文本。
 *
 * 并且，可以用来规避讨厌的 $。
 */
@DslMarker
annotation class ParadoxScriptTextBuilderDsl

/**
 * @see ParadoxScriptTextBuilderDsl
 */
inline fun <R> buildScriptText(block: ParadoxScriptTextBuilder.() -> R): R = ParadoxScriptTextBuilder.block()

/**
 * @see ParadoxScriptTextBuilderDsl
 */
@ParadoxScriptTextBuilderDsl
object ParadoxScriptTextBuilder {
    fun inlineMath(expression: String) = "@[ $expression ]"

    fun parameter(name: String) = "$${name}$"

    fun parameter(name: String, defaultValue: String) = "$${name}|${defaultValue}$"

    fun conditionalBlock(expression: String, block: String) = "[[${expression}] ${block} ]"

    fun conditionalBlock(expression: String, block: ParadoxScriptTextBuilder.() -> String) = "[[${expression}] ${block()} ]"
}
