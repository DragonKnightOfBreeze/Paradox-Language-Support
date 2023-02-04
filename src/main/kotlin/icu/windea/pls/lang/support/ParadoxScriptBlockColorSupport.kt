package icu.windea.pls.lang.support

import com.intellij.openapi.command.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import org.apache.commons.imaging.color.*
import java.awt.*

class ParadoxScriptBlockColorSupport : ParadoxColorSupport {
    override fun getElementFromToken(tokenElement: PsiElement): PsiElement? {
        val elementType = tokenElement.elementType
        if(elementType != LEFT_BRACE) return null
        return tokenElement.parent as? ParadoxScriptBlock
    }
    
    override fun getColor(element: PsiElement): Color? {
        if(element !is ParadoxScriptBlock) return null
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
    
    private fun doGetColor(element: ParadoxScriptBlock): Color? {
        val colorType = getColorType(element) ?: return null
        val colorArgs = getColorArgs(element) ?: return null
        return ParadoxColorHandler.getColor(colorType, colorArgs)
    }
    
    private fun getColorType(element: ParadoxScriptBlock): String? {
        val elementToGetOption: ParadoxScriptMemberElement? = when {
            element.isPropertyValue() -> element.parent as? ParadoxScriptProperty
            element.isBlockValue() -> element
            else -> null
        }
        if(elementToGetOption == null) return null
        return ParadoxColorHandler.getColorType(elementToGetOption)
    }
    
    private fun getColorArgs(element: ParadoxScriptBlock): List<String>? {
        return element.valueList
            .takeIf { (it.size == 3 || it.size == 4) && it.all { v -> v.isValidExpression() } }
            ?.map { it.resolved() ?: return null }
            ?.takeIf { it.all { v -> v is ParadoxScriptInt || v is ParadoxScriptFloat } }
            ?.map { it.value }
    }
    
    override fun setColor(element: PsiElement, color: Color) {
        if(element !is ParadoxScriptBlock) return
        try {
            return doSetColor(element, color)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            //ignored
        }
    }
    
    private fun doSetColor(element: ParadoxScriptBlock, color: Color) {
        val project = element.project
        val colorType = getColorType(element)
        val colorArgs = getColorArgs(element) ?: return //中断操作
        if(colorArgs.size != 3 && colorArgs.size != 4) return //中断操作
        val shouldBeRgba = color.alpha != 255 || colorArgs.size == 4
        val newText = when(colorType) {
            "rgb" -> {
                if(shouldBeRgba) {
                    "{ ${color.run { "$red $green $blue $alpha" }} }"
                } else {
                    "{ ${color.run { "$red $green $blue" }} }"
                }
            }
            "hsv" -> {
                val colorHsv = ColorConversions.convertRGBtoHSV(color.rgb shr 8)
                if(shouldBeRgba) {
                    "{ ${colorHsv.run { "${H.asFloat()} ${S.asFloat()} ${V.asFloat()} ${(color.alpha / 255f).asFloat()}" }} }"
                } else {
                    "{ ${colorHsv.run { "${H.asFloat()} ${S.asFloat()} ${V.asFloat()}" }} }"
                }
            }
            else -> null
        }
        if(newText == null) return
        val newBlock = ParadoxScriptElementFactory.createValue(project, newText)
        if(newBlock !is ParadoxScriptBlock) return
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(element.containingFile) ?: return
        val command = Runnable {
            element.replace(newBlock)
        }
        CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("script.command.changeColor.name"), null, document)
    }
    
    fun  Number.asFloat() = this.format(4)
}
