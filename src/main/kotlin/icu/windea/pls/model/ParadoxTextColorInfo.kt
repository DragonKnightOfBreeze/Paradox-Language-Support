package icu.windea.pls.model

import com.intellij.ui.ColorUtil
import com.intellij.util.ui.ColorIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextColorAwareElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import java.awt.Color

/**
 * 文本颜色的解析信息。
 *
 * @see ParadoxLocalisationTextColorAwareElement
 * @see ParadoxLocalisationColorfulText
 */
data class ParadoxTextColorInfo(
    val name: String,
    val r: Int,
    val g: Int,
    val b: Int,
    val gameType: ParadoxGameType,
) {
    @Volatile var element: ParadoxScriptProperty? = null // should be cached in associated PSI element, so holds directly

    @Suppress("UseJBColor")
    val color: Color = Color(r, g, b)
    val icon: ColorIcon = ColorIcon(16, color)
    val text: String = "$name = { $r $g $b }"
    val textWithColor: String = "<span style=\"color: #${ColorUtil.toHex(color, true)}\">$name</span> = { $r $g $b }"

    override fun toString(): String {
        return "ParadoxTextColorInfo(name=$name, color=$color, gameType=$gameType)"
    }
}
