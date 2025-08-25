package icu.windea.pls.ep.color

import com.intellij.openapi.command.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.util.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import java.awt.*

/**
 * 用于为字符串字面量（[ParadoxScriptString]）提供颜色的装订线图标。
 *
 * 示例 - 脚本片段：
 *
 * ```paradox_script
 * color = 0x2288E1
 * ```
 *
 * 示例 - 需要匹配的规则：
 *
 * ```cwt
 * ## color_type = hex
 * color = scalar
 * ```
 */
class ParadoxScriptStringColorProvider : ParadoxColorProvider {
    override fun getTargetElement(tokenElement: PsiElement): ParadoxScriptString? {
        if (tokenElement.elementType != STRING_TOKEN) return null
        if (tokenElement.prevSibling != null || tokenElement.nextSibling != null) return null
        return tokenElement.parent?.castOrNull()
    }

    override fun getColor(element: PsiElement): Color? {
        if (element !is ParadoxScriptString) return null
        return runCatchingCancelable { doGetColor(element) }.getOrNull()
    }

    override fun setColor(element: PsiElement, color: Color): Boolean {
        if (element !is ParadoxScriptString) return false
        runCatchingCancelable { doSetColor(element, color) }
        return true
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

/**
 * 用于为子句（[ParadoxScriptBlock]）提供颜色的装订线图标。
 *
 * 示例 - 脚本片段：
 *
 * ```paradox_script
 * color_rgb = { 34 136 225 }
 * color_hsv = { 208 0.849 0.882 }
 * ```
 *
 *
 * 示例 - 需要匹配的规则：
 *
 * ```cwt
 * 	## color_type = rgb
 * 	color_rgb = {
 * 		## cardinality = 3..3
 * 		int[0..255]
 * 	}
 * 	## color_type = hsv
 * 	color_hsv = {
 * 		## cardinality = 3..3
 * 		float
 * 	}
 * ```
 */
class ParadoxScriptBlockColorProvider : ParadoxColorProvider {
    override fun getTargetElement(tokenElement: PsiElement): ParadoxScriptBlock? {
        if (tokenElement.elementType != LEFT_BRACE) return null
        return tokenElement.parent?.castOrNull()
    }

    override fun getColor(element: PsiElement): Color? {
        if (element !is ParadoxScriptBlock) return null
        return runCatchingCancelable { doGetColorFromCache(element) }.getOrNull()
    }

    override fun setColor(element: PsiElement, color: Color): Boolean {
        if (element !is ParadoxScriptBlock) return false
        runCatchingCancelable { doSetColor(element, color) }
        return true
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

/**
 * 用于为颜色声明（[ParadoxScriptColor]）提供颜色的装订线图标。
 *
 * 示例 - 脚本片段：
 * ```paradox_script
 * color_rgb = rgb { 34 136 225 }
 * color_hsv = hsv { 208 0.849 0.882 }
 * ```
 *
 * 示例 - 需要匹配的规则：
 * ```cwt
 * 	color_rgb = color[rgb]
 * 	color_hsv = color[hsv]
 * ```
 */
class ParadoxScriptColorColorProvider : ParadoxColorProvider {
    override fun getTargetElement(tokenElement: PsiElement): ParadoxScriptColor? {
        if (tokenElement.elementType != COLOR_TOKEN) return null
        return tokenElement.parent?.castOrNull()
    }

    override fun getColor(element: PsiElement): Color? {
        if (element !is ParadoxScriptColor) return null
        return runCatchingCancelable { doGetColorFromCache(element) }.getOrNull()
    }

    override fun setColor(element: PsiElement, color: Color): Boolean {
        if (element !is ParadoxScriptColor) return false
        runCatchingCancelable { doSetColor(element, color) }
        return true
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
