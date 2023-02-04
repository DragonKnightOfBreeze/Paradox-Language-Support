package icu.windea.pls.lang.support

import com.intellij.openapi.command.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import java.awt.*

class ParadoxScriptStringColorSupport : ParadoxColorSupport {
    override fun supports(element: PsiElement): Boolean {
        return element is ParadoxScriptString
    }
    
    override fun getColor(element: PsiElement): Color? {
        element as ParadoxScriptString
        val hex = element.value.lowercase().removePrefixOrNull("0x") ?: return null
        return try {
            ParadoxColorHandler.getColor(hex)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            null
        }
    }
    
    override fun setColor(element: PsiElement, color: Color) {
        element as ParadoxScriptString
        try {
            return doSetColor(element, color)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            //ignored
        }
    }
    
    private fun doSetColor(element: ParadoxScriptString, color: Color) {
        val project = element.project
        val colorHex = color.toHex()
        val newText = "0x${colorHex}"
        val newString = ParadoxScriptElementFactory.createValue(project, newText)
        if(newString !is ParadoxScriptString) return
        val command = Runnable {
            element.replace(newString)
        }
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(element.containingFile) ?: return
        CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("script.command.changeColor.name"), null, document)
        documentManager.doPostponedOperationsAndUnblockDocument(document)
    }
}