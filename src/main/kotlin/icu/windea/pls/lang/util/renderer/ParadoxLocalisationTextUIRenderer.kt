package icu.windea.pls.lang.util.renderer

import com.intellij.util.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import java.awt.*
import javax.swing.*

object ParadoxLocalisationTextUIRenderer {
    //com.intellij.openapi.actionSystem.impl.ActionToolbarImpl.paintToImage

    fun render(element: ParadoxLocalisationProperty, color: Color? = null): JLabel? {
        val text = ParadoxLocalisationTextHtmlRenderer.render(element, color)
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

    fun render(text: String, color: Color? = null): JLabel? {
        if (text.isEmpty()) return null
        val label = JLabel()
        label.text = buildString {
            append("<html>")
            if (color == null) {
                append("<span>")
            } else {
                append("<span style=\"color: #").append(color.toHex()).append("\">")
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
