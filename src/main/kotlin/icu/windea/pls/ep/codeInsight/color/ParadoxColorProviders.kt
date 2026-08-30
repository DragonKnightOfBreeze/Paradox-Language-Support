package icu.windea.pls.ep.codeInsight.color

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.elementType
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.codeInsight.color.ParadoxColorFactory
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptString
import java.awt.Color

/**
 * 用于为字符串（[ParadoxScriptString]）提供颜色的装订线图标。
 *
 * 示例（规则片段）：
 *
 * ```cwt
 * ## color_type = hex
 * color = scalar
 * ```
 *
 * 示例（匹配的脚本片段）：
 *
 * ```paradox_script
 * color = 0xFF0000 # red
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
        val colorArg = ParadoxColorFactory.getColorArg(element) ?: return null
        val colorType = ParadoxColorFactory.getColorType(element) ?: return null
        if (colorType != "hex") return null
        return ParadoxColorFactory.getColor(colorArg)
    }

    private fun doSetColor(element: ParadoxScriptString, color: Color) {
        val colorArg = ParadoxColorFactory.getColorArg(element) ?: return
        val newColorArg = ParadoxColorFactory.getNewColorArg(colorArg, color) ?: return
        val file = element.containingFile ?: return
        val project = file.project
        val newText = newColorArg
        val newString = ParadoxScriptElementFactory.createValueFromText(project, newText)
        if (newString !is ParadoxScriptString) return
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(file) ?: return
        val command = Runnable {
            // element.replace(newString) // do not do this, element could be reused
            (element.node as CompositeElement).replaceAllChildrenToChildrenOf(newString.node)
        }
        CommandProcessor.getInstance().executeCommand(project, command, ChronicleBundle.message("script.command.changeColor.name"), null, document)
        documentManager.doPostponedOperationsAndUnblockDocument(document)
    }
}

/**
 * 用于为块（[ParadoxScriptBlock]）提供颜色的装订线图标。
 *
 * 示例（规则片段）：
 *
 * ```cwt
 * ## color_type = rgb
 * color_rgb = {
 *     ## cardinality = 3..4
 *     int[0..255]
 * }
 * ## color_type = hsv
 * color_hsv = {
 *     ## cardinality = 3..3
 *     float
 * }
 * ```
 *
 * 示例（匹配的脚本片段）：
 *
 * ```paradox_script
 * color_rgb = { 255 0 0 } # red
 * color_hsv = { 0 1.0 1.0 } # red
 * ```
 */
class ParadoxScriptBlockColorProvider : ParadoxColorProvider {
    override fun getTargetElement(tokenElement: PsiElement): ParadoxScriptBlock? {
        if (tokenElement.elementType != LEFT_BRACE) return null
        return tokenElement.parent?.castOrNull()
    }

    override fun getColor(element: PsiElement): Color? {
        if (element !is ParadoxScriptBlock) return null
        return CachedValuesManager.getCachedValue(element, ParadoxColorProvider.Keys.cachedColor) {
            ProgressManager.checkCanceled()
            val value = runCatchingCancelable { doGetColor(element) }.getOrNull()
            value.withDependencyItems(element)
        }
    }

    override fun setColor(element: PsiElement, color: Color): Boolean {
        if (element !is ParadoxScriptBlock) return false
        runCatchingCancelable { doSetColor(element, color) }
        return true
    }

    private fun doGetColor(element: ParadoxScriptBlock): Color? {
        val colorType = ParadoxColorFactory.getColorType(element) ?: return null
        val colorArgs = ParadoxColorFactory.getColorArgs(element) ?: return null
        return ParadoxColorFactory.getColor(colorType, colorArgs)
    }

    private fun doSetColor(element: ParadoxScriptBlock, color: Color) {
        val colorType = ParadoxColorFactory.getColorType(element) ?: return
        val colorArgs = ParadoxColorFactory.getColorArgs(element) ?: return
        val newColorArgs = ParadoxColorFactory.getNewColorArgs(colorType, colorArgs, color) ?: return
        val file = element.containingFile ?: return
        val project = file.project
        val newText = newColorArgs.joinToString(" ", "{ ", " }")
        val newBlock = ParadoxScriptElementFactory.createValueFromText(project, newText)
        if (newBlock !is ParadoxScriptBlock) return
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(file) ?: return
        val command = Runnable {
            // element.replace(newBlock) // do not do this, element could be reused
            (element.node as CompositeElement).replaceAllChildrenToChildrenOf(newBlock.node)
        }
        CommandProcessor.getInstance().executeCommand(project, command, ChronicleBundle.message("script.command.changeColor.name"), null, document)
        documentManager.doPostponedOperationsAndUnblockDocument(document)
    }
}

/**
 * 用于为颜色字段（[ParadoxScriptColor]）提供颜色的装订线图标。
 *
 * 示例（规则片段）：
 * ```cwt
 * 	color_field_rgb = color[rgb]
 * 	color_field_hsv = color[hsv]
 * ```
 *
 * 示例（匹配的脚本片段）：
 * ```paradox_script
 * color_field_rgb = rgb { 255 0 0 } # red
 * color_field_hsv = hsv { 0 1.0 1.0 } # red
 * ```
 */
class ParadoxScriptColorFieldColorProvider : ParadoxColorProvider {
    override fun getTargetElement(tokenElement: PsiElement): ParadoxScriptColor? {
        if (tokenElement.elementType != COLOR_TOKEN) return null
        return tokenElement.parent?.castOrNull()
    }

    override fun getColor(element: PsiElement): Color? {
        if (element !is ParadoxScriptColor) return null
        return CachedValuesManager.getCachedValue(element, ParadoxColorProvider.Keys.cachedColor) {
            ProgressManager.checkCanceled()
            val value = runCatchingCancelable { doGetColor(element) }.getOrNull()
            value.withDependencyItems(element)
        }
    }

    override fun setColor(element: PsiElement, color: Color): Boolean {
        if (element !is ParadoxScriptColor) return false
        runCatchingCancelable { doSetColor(element, color) }
        return true
    }

    private fun doGetColor(element: ParadoxScriptColor): Color? {
        val colorType = ParadoxColorFactory.getColorType(element) ?: return null
        val colorArgs = ParadoxColorFactory.getColorArgs(element) ?: return null
        return ParadoxColorFactory.getColor(colorType, colorArgs)
    }

    private fun doSetColor(element: ParadoxScriptColor, color: Color) {
        val colorType = ParadoxColorFactory.getColorType(element) ?: return
        val colorArgs = ParadoxColorFactory.getColorArgs(element) ?: return
        val newColorArgs = ParadoxColorFactory.getNewColorArgs(colorType, colorArgs, color) ?: return
        val file = element.containingFile ?: return
        val project = file.project
        val newText = newColorArgs.joinToString(" ", "$colorType { ", " }")
        val newColor = ParadoxScriptElementFactory.createValueFromText(project, newText)
        if (newColor !is ParadoxScriptColor) return
        val command = Runnable {
            // element.replace(newColor) // do not do this, element could be reused
            (element.node as CompositeElement).replaceAllChildrenToChildrenOf(newColor.node)
        }
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(file) ?: return
        CommandProcessor.getInstance().executeCommand(project, command, ChronicleBundle.message("script.command.changeColor.name"), null, document)
        documentManager.doPostponedOperationsAndUnblockDocument(document)
    }
}
