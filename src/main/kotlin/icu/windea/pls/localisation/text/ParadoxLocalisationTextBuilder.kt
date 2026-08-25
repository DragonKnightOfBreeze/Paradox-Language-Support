@file:Suppress("unused")

package icu.windea.pls.localisation.text

/**
 * 这个 DSL 提供了一组常见的构建方法，从而支持以字符串插值的风格，或者说类 DSL 风格，构建本地化文本。
 *
 * 并且，可以用来规避讨厌的 $ 以及其他特殊标记字符。
 */
@DslMarker
annotation class ParadoxLocalisationTextBuilderDsl

/**
 * @see ParadoxLocalisationTextBuilderDsl
 */
inline fun <R> buildLocalisationText(block: ParadoxLocalisationTextBuilder.() -> R): R = ParadoxLocalisationTextBuilder.block()

/**
 * @see ParadoxLocalisationTextBuilderDsl
 */
@ParadoxLocalisationTextBuilderDsl
object ParadoxLocalisationTextBuilder {
    fun colorfulText(colorId: String, text: String) = "§${colorId}${text}§!"

    fun parameter(name: String) = "$${name}$"

    fun parameter(name: String, argument: String) = "$${name}|${argument}$"

    fun scriptedVariableReference(name: String) = "$@${name}$"

    fun command(name: String) = "[${name}]"

    fun icon(name: String) = "£${name}£"

    fun icon(name: String, argument: String) = "£${name}|${argument}£"

    fun conceptCommand(name: String) = "['${name}']"

    fun conceptCommand(name: String, text: String) = "['${name}', ${text}]"

    fun textFormat(name: String, text: String) = "#${name} ${text}#!"

    fun textIcon(name: String) = "@${name}!"
}
