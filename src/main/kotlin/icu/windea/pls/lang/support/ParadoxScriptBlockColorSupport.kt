package icu.windea.pls.lang.support

import com.intellij.openapi.command.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import org.apache.commons.imaging.color.*
import java.awt.*

class ParadoxScriptBlockColorSupport : ParadoxColorSupport {
    override fun supports(element: PsiElement): Boolean {
        return element is ParadoxScriptBlock
    }
    
    override fun getColor(element: PsiElement): Color? {
        element as ParadoxScriptBlock
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
        val elementToGetOption = when {
            element.isPropertyValue() -> element.parent as? ParadoxScriptProperty
            element.isBlockValue() -> element
            else -> null
        }
        if(elementToGetOption == null) return null
        val configToGetOption = ParadoxCwtConfigHandler.resolveConfigs(elementToGetOption, allowDefinitionSelf = true)
            .firstOrNull()
        if(configToGetOption == null) return null
        return getColorType(configToGetOption)
    }
    
    private fun getColorArgs(element: ParadoxScriptBlock): List<String>? {
        return element.valueList
            .takeIf { (it.size == 3 || it.size == 4) && it.all { v -> v.isValidExpression() } }
            ?.map { it.resolved() ?: return null }
            ?.takeIf { it.all { v -> v is ParadoxScriptInt || v is ParadoxScriptFloat } }
            ?.map { it.value }
    }
    
    private fun getColorType(configToGetOption: CwtDataConfig<*>): String? {
        return configToGetOption.options?.find { it.key == "color_type" }?.stringValue
    }
    
    override fun setColor(element: PsiElement, color: Color) {
        element as ParadoxScriptBlock
        try {
            return doSetColor(element, color)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            //ignored
        }
    }
    
    private fun doSetColor(element: ParadoxScriptBlock, color: Color) {
        //FIXME 首次选择颜色后不关闭取色器，继续选择颜色，文档不会发生相应的变更，得到的document=null
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
                    "{ ${colorHsv.run { "$H $S $V ${color.alpha / 0f}" }} }"
                } else {
                    "{ ${colorHsv.run { "$H $S $V" }} }"
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
        documentManager.doPostponedOperationsAndUnblockDocument(document)
    }
}
