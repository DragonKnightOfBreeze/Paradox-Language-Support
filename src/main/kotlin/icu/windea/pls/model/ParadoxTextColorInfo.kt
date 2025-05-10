package icu.windea.pls.model

import com.intellij.psi.*
import com.intellij.ui.ColorUtil
import com.intellij.util.ui.*
import icu.windea.pls.script.psi.*
import java.awt.*

class ParadoxTextColorInfo(
    val name: String,
    val gameType: ParadoxGameType,
    val pointer: SmartPsiElementPointer<ParadoxScriptProperty>,
    val r: Int,
    val g: Int,
    val b: Int
) {
    @Suppress("UseJBColor")
    val color: Color = Color(r, g, b)
    val icon: ColorIcon = ColorIcon(16, color)
    val text: String = "$name = { $r $g $b }"
    val textWithColor: String = "<span style=\"color: #${ColorUtil.toHex(color, true)}\">$name</span> = { $r $g $b }"

}
