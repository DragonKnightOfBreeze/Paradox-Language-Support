package icu.windea.pls.lang.support

import com.intellij.openapi.command.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import org.apache.commons.imaging.color.*
import java.awt.*

class ParadoxScriptColorColorSupport : ParadoxColorSupport {
    override fun supports(element: PsiElement): Boolean {
        return element is ParadoxScriptColor
    }
    
    override fun getColor(element: PsiElement): Color? {
        element as ParadoxScriptColor
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedColorKey) {
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
    
    override fun setColor(element: PsiElement, color: Color) {
        element as ParadoxScriptColor
        try {
            return doSetColor(element, color)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            //ignored
        }
    }
    
    private fun doSetColor(element: ParadoxScriptColor, color: Color) {
        val project = element.project
        val colorType = element.colorType
        val colorArgs = element.colorArgs
        if(colorArgs.size != 3 && colorArgs.size != 4) return //中断操作
        val shouldBeRgba = color.alpha != 255 || colorArgs.size == 4
        val newText = when(colorType) {
            "rgb" -> {
                if(shouldBeRgba) {
                    "rgb { ${color.run { "$red $green $blue $alpha" }} }"
                } else {
                    "rgb { ${color.run { "$red $green $blue" }} }"
                }
            }
            "hsv" -> {
                val colorHsv = ColorConversions.convertRGBtoHSV(color.rgb shr 8)
                if(shouldBeRgba) {
                    "hsv { ${colorHsv.run { "$H $S $V ${color.alpha / 0f}" }} }"
                } else {
                    "hsv { ${colorHsv.run { "$H $S $V" }} }"
                }
            }
            else -> null
        }
        if(newText == null) return
        val newColor = ParadoxScriptElementFactory.createValue(project, newText)
        if(newColor !is ParadoxScriptColor) return
        val command = Runnable {
            element.replace(newColor)
        }
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(element.containingFile) ?: return
        CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("script.command.changeColor.name"), null, document)
        documentManager.doPostponedOperationsAndUnblockDocument(document)
    }
}
