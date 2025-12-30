package icu.windea.pls.lang.util.renderers

import com.intellij.ui.ColorUtil
import com.intellij.util.ui.JBUI
import java.awt.Color
import javax.swing.JLabel

/**
 * 用于将本地化文本渲染为 UI 文本。
 */
class ParadoxLocalisationTextUIRenderer(
    val color: Color? = null
) {
    // com.intellij.openapi.actionSystem.impl.ActionToolbarImpl.paintToImage

    fun render(text: String): JLabel {
        return doRender(doGetFinalText(text))
    }

    private fun doGetFinalText(text: String): String = buildString {
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

    private fun doRender(text: String): JLabel {
        val label = JLabel()
        label.text = text
        label.border = JBUI.Borders.empty()
        label.size = label.preferredSize
        label.isOpaque = false
        return label
    }
}
