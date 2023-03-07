package icu.windea.pls.tool.localisation

import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.util.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import java.awt.*
import java.awt.image.*
import javax.swing.*

@Suppress("unused")
object ParadoxLocalisationTextUIRender {
    private val defaultTextAttributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, Color.WHITE)
    
    //com.intellij.openapi.actionSystem.impl.ActionToolbarImpl.paintToImage
    
    fun render(text: String): JLabel? {
        if(text.isEmpty()) return null
        val label = JLabel()
        label.text = buildString {
            append("<html>")
            append("<span style=\"color: #fff\">")
            append(text)
            append("</span>")
            append("</html>")
        }
       label.border = JBUI.Borders.empty()
       label.size = label.preferredSize
       label.isOpaque = false
        return label
    }
    
    fun renderImage(text: String): Image? {
        val label = render(text) ?: return null
        val image = UIUtil.createImage(label, label.width, label.height, BufferedImage.TYPE_INT_ARGB_PRE)
        UIUtil.useSafely(image.graphics) { label.paint(it) }
        return image
    }
    
    fun render(text: String, colorId: String, contextElement: PsiElement): JLabel? {
        if(text.isEmpty()) return null
        val label = JLabel()
        val color = ParadoxTextColorHandler.getInfo(colorId, contextElement.project, contextElement)?.color
        if(color != null) {
            label.text = buildString {
                val colorHex = color.toHex()
                append("<html>")
                append("<span style=\"color: #").append(colorHex).append("\">")
                append(text)
                append("</span>")
                append("</html>")
            }
        } else {
            label.text = buildString {
                append("<html>")
                append("<span style=\"color: #fff\">")
                append(text)
                append("</span>")
                append("</html>")
            }
        }
        label.border = JBUI.Borders.empty()
        label.size = label.preferredSize
        label.isOpaque = false
        return label
    }
    
    fun renderImage(text: String, colorId: String, contextElement: PsiElement): Image? {
        val label = render(text, colorId, contextElement) ?: return null
        return label.toImage()
    }
    
    fun render(element: ParadoxLocalisationProperty): JLabel? {
        val text = ParadoxLocalisationTextRenderer.render(element)
        if(text.isEmpty()) return null
        val label = JLabel()
        label.text = buildString {
            append("<html>")
            append("<span style=\"color: #fff\">")
            append(text)
            append("</span>")
            append("</html>")
        }
        label.border = JBUI.Borders.empty()
        label.size = label.preferredSize
        label.isOpaque = false
        return label
    }
    
    fun renderImage(element: ParadoxLocalisationProperty): Image? {
        val label = render(element) ?: return null
        return label.toImage()
    }
}