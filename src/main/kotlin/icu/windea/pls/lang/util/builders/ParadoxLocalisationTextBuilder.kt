package icu.windea.pls.lang.util.builders

@Suppress("unused")
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
