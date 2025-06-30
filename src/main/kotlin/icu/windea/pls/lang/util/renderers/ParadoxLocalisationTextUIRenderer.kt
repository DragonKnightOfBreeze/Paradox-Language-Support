package icu.windea.pls.lang.util.renderers

import com.intellij.ui.*
import com.intellij.util.ui.*
import icu.windea.pls.localisation.psi.*
import java.awt.*
import javax.swing.*

class ParadoxLocalisationTextUIRenderer(
    val color: Color? = null
) {
    //com.intellij.openapi.actionSystem.impl.ActionToolbarImpl.paintToImage

    fun render(element: ParadoxLocalisationProperty): JLabel? {
        val text = ParadoxLocalisationTextHtmlRenderer(color = color).render(element)
        if (text.isEmpty()) return null
        val label = JLabel()
        label.text = buildString {
            append("<html>")
            append(text)
            append("</html>")
        }
        label.border = JBUI.Borders.empty()
        label.size = label.preferredSize
        label.isOpaque = false
        return label
    }

    fun render(text: String): JLabel? {
        if (text.isEmpty()) return null
        val label = JLabel()
        label.text = buildString {
            append("<html>")
            if (color == null) {
                append("<span>")
            } else {
                append("<span style=\"color: #").append(ColorUtil.toHex(color, true)).append("\">")
            }
            append(text)
            append("</span>")
            append("</html>")
        }
        label.border = JBUI.Borders.empty()
        label.size = label.preferredSize
        label.isOpaque = false
        return label
    }
}
