package icu.windea.pls.lang.color

import com.intellij.openapi.command.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import java.awt.*

class ParadoxScriptStringColorSupport : ParadoxColorSupport {
    override fun getColor(element: PsiElement): Color? {
        if(element !is ParadoxScriptString) return null
        return try {
            doGetColor(element)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            null
        }
    }
    
    private fun doGetColor(element: ParadoxScriptString): Color? {
        val hex = element.value.lowercase().removePrefixOrNull("0x") ?: return null
        if(hex.length != 6 && hex.length != 8) return null
        val colorType = ParadoxColorHandler.getColorType(element) ?: return null
        if(colorType != "hex") return null
        return ParadoxColorHandler.getColor(hex)
    }
    
    override fun setColor(element: PsiElement, color: Color): Boolean {
        if(element !is ParadoxScriptString) return false
        try {
            doSetColor(element, color)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            //ignored
        }
        return true
    }
    
    private fun doSetColor(element: ParadoxScriptString, color: Color) {
        val project = element.project
        val colorHex = color.toHex()
        val newText = "0x${colorHex}"
        val newString = ParadoxScriptElementFactory.createValue(project, newText)
        if(newString !is ParadoxScriptString) return
        val command = Runnable {
            //element.replace(newString) //do not do this, element could be reused
            (element.node as CompositeElement).replaceAllChildrenToChildrenOf(newString.node)
        }
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(element.containingFile) ?: return
        CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("script.command.changeColor.name"), null, document)
        //documentManager.doPostponedOperationsAndUnblockDocument(document)
    }
}