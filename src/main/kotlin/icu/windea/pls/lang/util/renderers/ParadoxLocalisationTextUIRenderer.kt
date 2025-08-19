package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.application.*
import com.intellij.ui.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.model.constants.*
import kotlinx.coroutines.*
import java.awt.*
import javax.swing.*

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
            label.text = readAction { lazyText() }
            label.size = label.preferredSize
        }
        return label
    }
}
