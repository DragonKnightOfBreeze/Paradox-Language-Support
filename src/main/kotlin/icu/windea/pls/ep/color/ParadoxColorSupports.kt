package icu.windea.pls.ep.color

import com.intellij.openapi.command.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.ep.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import java.awt.*

class ParadoxScriptColorColorSupport : ParadoxColorSupport {
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
        documentManager.doPostponedOperationsAndUnblockDocument(document)
    }
    
    fun Number.asFloat() = this.format(-4)
}

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
        documentManager.doPostponedOperationsAndUnblockDocument(document)
    }
}

class ParadoxScriptBlockColorSupport : ParadoxColorSupport {
    override fun getColor(element: PsiElement): Color? {
        if(element !is ParadoxScriptBlock) return null
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
    
    private fun doGetColor(element: ParadoxScriptBlock): Color? {
        val colorType = getColorType(element)
        val colorArgs = getColorArgs(element)
        if(colorType == null || colorArgs == null) return null
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
            //.takeIf { (it.size == 3 || it.size == 4) && it.all { v -> v.isValidExpression() } }
            .takeIf { (it.size == 3 || it.size == 4) && it.all { v -> v.isValidExpression() } }
            ?.map { it.resolved() ?: return null }
            ?.takeIf { it.all { v -> v is ParadoxScriptInt || v is ParadoxScriptFloat } }
            ?.map { it.value }
    }
    
    override fun setColor(element: PsiElement, color: Color): Boolean {
        if(element !is ParadoxScriptBlock) return false
        try {
            doSetColor(element, color)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            //ignored
        }
        return true
    }
    
    private fun doSetColor(element: ParadoxScriptBlock, color: Color) {
        val project = element.project
        val colorType = getColorType(element)
        val colorArgs = getColorArgs(element)
        if(colorType == null || colorArgs == null) return
        if(colorArgs.size != 3 && colorArgs.size != 4) return //中断操作
        val addAlpha = color.alpha != 255 || colorArgs.size == 4
        val newText = when(colorType) {
            "rgb" -> {
                val (r, g, b, a) = color
                when {
                    addAlpha -> "{ $r $g $b $a }"
                    else -> "{ $r $g $b }"
                }
            }
            "hsv" -> {
                val (r, g, b, a) = color
                val (h, s, v) = Color.RGBtoHSB(r, g, b, null)
                when {
                    addAlpha -> "{ ${h.asFloat()} ${s.asFloat()} ${v.asFloat()} ${(a / 255f).asFloat()} }"
                    else -> "{ ${h.asFloat()} ${s.asFloat()} ${v.asFloat()} }"
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
            //element.replace(newBlock) //do not do this, element could be reused
            (element.node as CompositeElement).replaceAllChildrenToChildrenOf(newBlock.node)
        }
        CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("script.command.changeColor.name"), null, document)
        documentManager.doPostponedOperationsAndUnblockDocument(document)
    }
    
    fun Number.asFloat() = this.format(-4)
}