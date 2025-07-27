package icu.windea.pls.ep.color

import com.intellij.openapi.command.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.util.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import java.awt.*

class ParadoxScriptStringColorSupport : ParadoxColorSupport {
    override fun getColor(element: PsiElement): Color? {
        val targetElement = getTargetElement(element) ?: return null
        return runCatchingCancelable { doGetColor(targetElement) }.getOrNull()
    }

    override fun setColor(element: PsiElement, color: Color): Boolean {
        val targetElement = getTargetElement(element) ?: return false
        runCatchingCancelable { doSetColor(targetElement, color) }
        return true
    }

    private fun getTargetElement(element: PsiElement): ParadoxScriptString? {
        if(element.elementType != ParadoxScriptElementTypes.STRING_TOKEN) return null
        if(element.prevSibling != null || element.nextSibling != null) return null
        return element.parent?.castOrNull()
    }

    private fun doGetColor(element: ParadoxScriptString): Color? {
        val hex = element.value.lowercase().removePrefixOrNull("0x") ?: return null
        if (hex.length != 6 && hex.length != 8) return null
        val colorType = ParadoxColorManager.getColorType(element) ?: return null
        if (colorType != "hex") return null
        return ParadoxColorManager.getColor(hex)
    }

    private fun doSetColor(element: ParadoxScriptString, color: Color) {
        val project = element.project
        val newText = "0x${ColorUtil.toHex(color, true)}"
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
        val targetElement = getTargetElement(element) ?: return null
        return runCatchingCancelable { doGetColorFromCache(targetElement) }.getOrNull()
    }

    override fun setColor(element: PsiElement, color: Color): Boolean {
        val targetElement = getTargetElement(element) ?: return false
        runCatchingCancelable { doSetColor(targetElement, color) }
        return true
    }

    private fun getTargetElement(element: PsiElement): ParadoxScriptBlock? {
        if(element.elementType != ParadoxScriptElementTypes.LEFT_BRACE) return null
        return element.parent?.castOrNull()
    }

    private fun doGetColorFromCache(element: ParadoxScriptBlock): Color? {
        return CachedValuesManager.getCachedValue(element, ParadoxColorManager.Keys.cachedColor) {
            val value = doGetColor(element)
            value.withDependencyItems(element)
        }
    }

    private fun doGetColor(element: ParadoxScriptBlock): Color? {
        val colorType = getColorType(element)
        val colorArgs = getColorArgs(element)
        if (colorType == null || colorArgs == null) return null
        return ParadoxColorManager.getColor(colorType, colorArgs)
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
}

class ParadoxScriptColorColorSupport : ParadoxColorSupport {
    override fun getColor(element: PsiElement): Color? {
        val targetElement = getTargetElement(element) ?: return null
        return runCatchingCancelable { doGetColorFromCache(targetElement) }.getOrNull()
    }

    override fun setColor(element: PsiElement, color: Color): Boolean {
        val targetElement = getTargetElement(element) ?: return false
        runCatchingCancelable { doSetColor(targetElement, color) }
        return true
    }

    private fun getTargetElement(element: PsiElement): ParadoxScriptColor? {
        if(element.elementType != ParadoxScriptElementTypes.COLOR_TOKEN) return null
        return element.parent?.castOrNull()
    }

    private fun doGetColorFromCache(element: ParadoxScriptColor): Color? {
        return CachedValuesManager.getCachedValue(element, ParadoxColorManager.Keys.cachedColor) {
            val value = doGetColor(element)
            value.withDependencyItems(element)
        }
    }

    private fun doGetColor(element: ParadoxScriptColor): Color? {
        val colorType = element.colorType
        val colorArgs = element.colorArgs
        return ParadoxColorManager.getColor(colorType, colorArgs)
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
}
