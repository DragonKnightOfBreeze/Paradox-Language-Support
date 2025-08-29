package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.application.UI
import com.intellij.openapi.application.readAction
import com.intellij.ui.ColorUtil
import com.intellij.util.ui.JBUI
import icu.windea.pls.PlsFacade
import icu.windea.pls.model.constants.PlsStringConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Color
import javax.swing.JLabel

class ParadoxLocalisationTextUIRenderer(
    val color: Color? = null
) {
    //com.intellij.openapi.actionSystem.impl.ActionToolbarImpl.paintToImage

    fun render(text: String): JLabel {
        return doRender(doGetFinalText(text))
    }

    fun render(text: () -> String): JLabel {
        return doLazyRender { doGetFinalText(text()) }
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

    private fun doLazyRender(lazyText: () -> String): JLabel {
        val label = JLabel()
        label.text = PlsStringConstants.loadingMarker
        label.border = JBUI.Borders.empty()
        label.size = label.preferredSize
        label.isOpaque = false
        PlsFacade.getCoroutineScope().launch {
            val text = readAction { lazyText() }
            withContext(Dispatchers.UI) {
                label.text = text
                label.size = label.preferredSize
            }
        }
        return label
    }
}
