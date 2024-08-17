package icu.windea.pls.model

import com.intellij.psi.*
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
    val color: Color = Color(r, g, b)
    val icon = ColorIcon(16, color)
    val text = "$name = { $r $g $b }"
}
