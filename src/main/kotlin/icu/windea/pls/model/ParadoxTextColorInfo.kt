package icu.windea.pls.model

import com.intellij.psi.SmartPsiElementPointer
import com.intellij.ui.ColorUtil
import com.intellij.util.ui.ColorIcon
import icu.windea.pls.script.psi.ParadoxScriptProperty
import java.awt.Color

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
