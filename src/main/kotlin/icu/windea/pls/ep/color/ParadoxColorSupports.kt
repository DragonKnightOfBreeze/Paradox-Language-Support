package icu.windea.pls.ep.color

import com.intellij.openapi.command.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import java.awt.*

class ParadoxScriptStringColorSupport : ParadoxColorSupport {
    override fun getColor(element: PsiElement): Color? {
        if (element !is ParadoxScriptString) return null
        return runCatchingCancelable { doGetColor(element) }.getOrNull()
    }

    private fun doGetColor(element: ParadoxScriptString): Color? {
        val hex = element.value.lowercase().removePrefixOrNull("0x") ?: return null
        if (hex.length != 6 && hex.length != 8) return null
        val colorType = ParadoxColorManager.getColorType(element) ?: return null
        if (colorType != "hex") return null
        return ParadoxColorManager.getColor(hex)
    }

    override fun setColor(element: PsiElement, color: Color): Boolean {
        if (element !is ParadoxScriptString) return false
        runCatchingCancelable { doSetColor(element, color) }
        return true
    }

    private fun doSetColor(element: ParadoxScriptString, color: Color) {
        val project = element.project
        val colorHex = color.toHex()
        val newText = "0x${colorHex}"
        val newString = ParadoxScriptElementFactory.createValue(project, newText)
        if (newString !is ParadoxScriptString) return
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
        if (element !is ParadoxScriptBlock) return null
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedColor) {
            val value = runCatchingCancelable { doGetColor(element) }.getOrNull()
            CachedValueProvider.Result.create(value, element)
        }
    }

    private fun doGetColor(element: ParadoxScriptBlock): Color? {
        val colorType = getColorType(element)
        val colorArgs = getColorArgs(element)
        if (colorType == null || colorArgs == null) return null
        return ParadoxColorManager.getColor(colorType, colorArgs)
    }

    private fun getColorType(element: ParadoxScriptBlock): String? {
        val elementToGetOption: ParadoxScriptMemberElement? = when {
            element.isPropertyValue() -> element.parent as? ParadoxScriptProperty
            element.isBlockMember() -> element
            else -> null
        }
        if (elementToGetOption == null) return null
        return ParadoxColorManager.getColorType(elementToGetOption)
    }

    private fun getColorArgs(element: ParadoxScriptBlock): List<String>? {
        return element.valueList
            .takeIf { (it.size == 3 || it.size == 4) && it.all { v -> v.isValidExpression() } }
            ?.map { it.resolved() ?: return null }
            ?.takeIf { it.all { v -> v is ParadoxScriptInt || v is ParadoxScriptFloat } }
            ?.map { it.value }
    }

    override fun setColor(element: PsiElement, color: Color): Boolean {
        if (element !is ParadoxScriptBlock) return false
        runCatchingCancelable { doSetColor(element, color) }
        return true
    }

    private fun doSetColor(element: ParadoxScriptBlock, color: Color) {
        val project = element.project
        val colorType = getColorType(element)
        val colorArgs = getColorArgs(element)
        if (colorType == null || colorArgs == null) return
        val newColorArgs = ParadoxColorManager.getNewColorArgs(colorType, colorArgs, color) ?: return
        val newText = newColorArgs.joinToString(" ", "{ ", " }")
        val newBlock = ParadoxScriptElementFactory.createValue(project, newText)
        if (newBlock !is ParadoxScriptBlock) return
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(element.containingFile) ?: return
        val command = Runnable {
            //element.replace(newBlock) //do not do this, element could be reused
            (element.node as CompositeElement).replaceAllChildrenToChildrenOf(newBlock.node)
        }
        CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("script.command.changeColor.name"), null, document)
        documentManager.doPostponedOperationsAndUnblockDocument(document)
    }

    private fun Number.asFloat() = this.format(-4)
}

class ParadoxScriptColorColorSupport : ParadoxColorSupport {
    override fun getColor(element: PsiElement): Color? {
        if (element !is ParadoxScriptColor) return null
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedColor) {
            val value = runCatchingCancelable { doGetColor(element) }.getOrNull()
            CachedValueProvider.Result.create(value, element)
        }
    }

    private fun doGetColor(element: ParadoxScriptColor): Color? {
        val colorType = element.colorType
        val colorArgs = element.colorArgs
        return ParadoxColorManager.getColor(colorType, colorArgs)
    }

    override fun setColor(element: PsiElement, color: Color): Boolean {
        if (element !is ParadoxScriptColor) return false
        runCatchingCancelable { doSetColor(element, color) }
        return true
    }

    private fun doSetColor(element: ParadoxScriptColor, color: Color) {
        val project = element.project
        val colorType = element.colorType
        val colorArgs = element.colorArgs
        val newColorArgs = ParadoxColorManager.getNewColorArgs(colorType, colorArgs, color) ?: return
        val newText = newColorArgs.joinToString(" ", "$colorType { ", " }")
        val newColor = ParadoxScriptElementFactory.createValue(project, newText)
        if (newColor !is ParadoxScriptColor) return
        val command = Runnable {
            //element.replace(newColor) //do not do this, element could be reused
            (element.node as CompositeElement).replaceAllChildrenToChildrenOf(newColor.node)
        }
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(element.containingFile) ?: return
        CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("script.command.changeColor.name"), null, document)
        documentManager.doPostponedOperationsAndUnblockDocument(document)
    }

    private fun Number.asFloat() = this.format(-4)
}
