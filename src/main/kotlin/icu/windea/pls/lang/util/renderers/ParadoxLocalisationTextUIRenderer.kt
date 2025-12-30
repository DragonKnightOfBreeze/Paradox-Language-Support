package icu.windea.pls.lang.util.renderers

import com.intellij.ui.ColorUtil
import com.intellij.util.ui.JBUI
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextUIRenderer.*
import java.awt.Color
import javax.swing.JLabel

/**
 * 用于将本地化文本渲染为 UI 文本。
 */
class ParadoxLocalisationTextUIRenderer : ParadoxRenderer<String, Context, String> {
    data class Context(
        var builder: StringBuilder = StringBuilder()
    )

    var color: Color? = null

    fun withColor(color: Color?) = apply { this.color = color }

    override fun initContext(): Context {
        return Context()
    }

    override fun render(input: String, context: Context): String {
        return renderText(input)
    }

    fun renderText(text: String, context: Context = initContext()): String {
        return with(context) { renderInternal(text) }
    }

    fun renderLabel(text: String, context: Context = initContext()): JLabel {
        val label = JLabel()
        label.text = renderText(text, context)
        label.border = JBUI.Borders.empty()
        label.size = label.preferredSize
        label.isOpaque = false
        return label
    }

    // com.intellij.openapi.actionSystem.impl.ActionToolbarImpl.paintToImage

    context(context: Context)
    private fun renderInternal(text: String): String {
        context.builder.append("<html>")
        val color = color
        if (color == null) {
            context.builder.append("<span>")
        } else {
            context.builder.append("<span style=\"color: #").append(ColorUtil.toHex(color, true)).append("\">")
        }
        context.builder.append(text)
        context.builder.append("</span>")
        context.builder.append("</html>")
        return context.builder.toString()
    }
}
