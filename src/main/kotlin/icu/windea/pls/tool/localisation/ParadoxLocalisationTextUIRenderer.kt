package icu.windea.pls.tool.localisation

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

@Suppress("unused")
object ParadoxLocalisationTextUIRenderer {
    //com.intellij.openapi.actionSystem.impl.ActionToolbarImpl.paintToImage
    
    fun render(element: ParadoxLocalisationProperty, colorHex: String? = null): JLabel? {
        val text = ParadoxLocalisationTextRenderer.render(element)
        if(text.isEmpty()) return null
        val label = JLabel()
        label.text = buildString {
            append("<html>")
            if(colorHex == null) append("<span>") else append("<span style=\"color: $colorHex\">")
            append(text)
            append("</span>")
            append("</html>")
        }
        label.border = JBUI.Borders.empty()
        label.size = label.preferredSize
        label.isOpaque = false
        return label
    }
    
    fun render(text: String, colorHex: String? = null): JLabel? {
        if(text.isEmpty()) return null
        val label = JLabel()
        label.text = buildString {
            append("<html>")
            if(colorHex == null) append("<span>") else append("<span style=\"color: $colorHex\">")
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
                append("<span>")
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
}