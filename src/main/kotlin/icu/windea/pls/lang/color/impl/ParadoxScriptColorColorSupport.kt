package icu.windea.pls.lang.color.impl

import com.intellij.openapi.command.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.color.*
import icu.windea.pls.script.psi.*
import java.awt.*

class ParadoxScriptColorColorSupport : ParadoxColorSupport {
    companion object {
        val INSTANCE = ParadoxScriptColorColorSupport()
    }
    
    override fun getColor(element: PsiElement): Color? {
        if(element !is ParadoxScriptColor) return null
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedColor) {
            val value = try {
                doGetColor(element)
            } catch(e: Exception) {
                if(e is ProcessCanceledException) throw e
                null
            }
            CachedValueProvider.Result.create(value, element)
        }
    }
    
    private fun doGetColor(element: ParadoxScriptColor): Color? {
        val colorType = element.colorType
        val colorArgs = element.colorArgs
        return ParadoxColorHandler.getColor(colorType, colorArgs)
    }
    
    override fun setColor(element: PsiElement, color: Color): Boolean {
        if(element !is ParadoxScriptColor) return false
        try {
            doSetColor(element, color)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            //ignored
        }
        return true
    }
    
    private fun doSetColor(element: ParadoxScriptColor, color: Color) {
        val project = element.project
        val colorType = element.colorType
        val colorArgs = element.colorArgs
        if(colorArgs.size != 3 && colorArgs.size != 4) return //中断操作
        val shouldBeRgba = color.alpha != 255 || colorArgs.size == 4
        val newText = when(colorType) {
            "rgb" -> {
                val (r, g, b, a) = color
                when {
                    shouldBeRgba -> "rgb { $r $g $b $a }"
                    else -> "rgb { $r $g $b }"
                }
            }
            "hsv" -> {
                val (r, g, b, a) = color
                val (h, s, v) = Color.RGBtoHSB(r, g, b, null)
                when {
                    shouldBeRgba -> "hsv { ${h.asFloat()} ${s.asFloat()} ${v.asFloat()} ${(a / 255f).asFloat()} }"
                    else -> "hsv { ${h.asFloat()} ${s.asFloat()} ${v.asFloat()} }"
                }
            }
            else -> null
        }
        if(newText == null) return
        val newColor = ParadoxScriptElementFactory.createValue(project, newText)
        if(newColor !is ParadoxScriptColor) return
        val command = Runnable {
            //element.replace(newColor) //do not do this, element could be reused
            (element.node as CompositeElement).replaceAllChildrenToChildrenOf(newColor.node)
        }
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(element.containingFile) ?: return
        CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("script.command.changeColor.name"), null, document)
        //documentManager.doPostponedOperationsAndUnblockDocument(document)
    }
    
    fun Number.asFloat() = this.format(-4)
}
