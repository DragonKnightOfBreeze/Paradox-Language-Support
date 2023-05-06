package icu.windea.pls.tool.localisation

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import java.awt.*
import javax.swing.*

@Suppress("unused")
object ParadoxLocalisationTextUIRenderer {
    //com.intellij.openapi.actionSystem.impl.ActionToolbarImpl.paintToImage
    
    fun render(element: ParadoxLocalisationProperty, color: Color? = null): JLabel? {
        val text = ParadoxLocalisationTextHtmlRenderer.render(element, color)
        if(text.isEmpty()) return null
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
        if(text.isEmpty()) return null
        val label = JLabel()
        label.text = buildString {
            append("<html>")
            if(color == null) {
                append("<span>")
            } else {
                append("<span style=\"color: ").append(color.toHex()).append("\">")
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
    
    fun render(text: String, colorId: String, project: Project, contextElement: PsiElement? = null): JLabel? {
        if(text.isEmpty()) return null
        val label = JLabel()
        val color = ParadoxTextColorHandler.getInfo(colorId, project, contextElement)?.color
        label.text = buildString {
            append("<html>")
            if(color == null) {
                append("<span>")
            } else {
                append("<span style=\"color: ").append(color.toHex()).append("\">")
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