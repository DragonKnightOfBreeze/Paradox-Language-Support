package icu.windea.pls.lang

import com.intellij.openapi.command.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import org.apache.commons.imaging.color.*
import java.awt.*

object ParadoxColorHandler {
    val colorTypeKey = Key.create<String>("paradox.colorType")
    
    // rgb { $r $g $b }
    // rgb { $r $g $b $a }
    // r,g,b,a - int[0..255]
    
    // hsv { $h $s $v }
    // hsv { $h $s $v $a }
    // h,s,v,a - int[0..255] or float[0.0..1.0]
    
    @JvmStatic
    fun getColor(element: PsiElement): Color? {
        return getColorFromCache(element)
    }
    
    private fun getColorFromCache(element: PsiElement): Color? {
        if(element !is ParadoxScriptColor && element !is ParadoxScriptBlock) return null
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
    
    private fun doGetColor(element: PsiElement): Color? {
        return when {
            element is ParadoxScriptColor -> doGetColor(element)
            element is ParadoxScriptBlock -> doGetColor(element)
            else -> null
        }
    }
    
    private fun doGetColor(element: ParadoxScriptColor): Color? {
        val colorType = element.colorType
        val colorArgs = element.colorArgs
        return doGetColor(colorType, colorArgs)
    }
    
    private fun doGetColor(element: ParadoxScriptBlock): Color? {
        val colorType = getColorType(element) ?: return null
        val colorArgs = getColorArgs(element) ?: return null
        return doGetColor(colorType, colorArgs)
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
    
    
    private fun getColorArgs(element: ParadoxScriptBlock) = element.valueList
        .takeIf { (it.size == 3 || it.size == 4) && it.all { v -> v.isValidExpression() } }
        ?.map { it.resolved() ?: return null }
        ?.takeIf { it.all { v -> v is ParadoxScriptInt || v is ParadoxScriptFloat } }
        ?.map { it.value }
    
    private fun getColorType(configToGetOption: CwtDataConfig<*>): String? {
        return configToGetOption.getOrPutUserData(colorTypeKey) {
            configToGetOption.options?.find { it.key == "color_type" }?.stringValue.orEmpty()
        }.takeIfNotEmpty()
    }
    
    private fun doGetColor(colorType: String, colorArgs: List<String>): Color? {
        when(colorType) {
            "rgb" -> {
                if(colorArgs.size != 3 && colorArgs.size != 4) throw IllegalStateException()
                val color = colorArgs.map { it.toInt() }.let { Color(it[0], it[1], it[2], it.getOrNull(3) ?: 255) }
                return color
            }
            "hsv" -> {
                if(colorArgs.size != 3 && colorArgs.size != 4) throw IllegalStateException()
                //args.takeIf { it.size == 3 }?.map { it.toFloat() }?.let {  }
                val colorRgb = colorArgs.map { it.toDouble() }.let { ColorConversions.convertHSVtoRGB(it[0], it[1], it[2]) }
                val colorRgba = (colorRgb shl 8) + ((colorArgs.getOrNull(3)?.toFloatOrNull() ?: 1.0f) * 255).toInt()
                val color = Color(colorRgba)
                return color
            }
            else -> {
                return null
            }
        }
    }
    
    @JvmStatic
    fun setColor(element: ParadoxScriptValue, color: Color) {
        try {
            return doSetColor(element, color)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            //ignored
        }
    }
    
    private fun doSetColor(element: ParadoxScriptValue, color: Color) {
        when {
            element is ParadoxScriptColor -> doSetColor(element, color)
            element is ParadoxScriptBlock -> doSetColor(element, color)
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
                    "hsv { ${colorHsv.run { "$H $S $V ${color.alpha.asFloat()}" }} }"
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
        val document = PsiDocumentManager.getInstance(project).getDocument(element.containingFile)
        CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("script.command.changeColor.name"), null, document)
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
                    "{ ${colorHsv.run { "$H $S $V ${color.alpha.asFloat()}" }} }"
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
        documentManager.doPostponedOperationsAndUnblockDocument(document)
        CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("script.command.changeColor.name"), null, document)
    }
    
    private fun Int.asFloat(): Float {
        return (this / 255.0F)
    }
}