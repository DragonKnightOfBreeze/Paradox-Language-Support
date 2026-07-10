package icu.windea.pls.lang.codeInsight.editorActions

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxMarkerManager

/**
 * 用于在脚本文件和本地化文件中提供基于光标位置的代码补全。
 *
 * - 当光标位置是复杂表达式中成对的标记节点（[ParadoxMarkerNode]）的开标记时，自动补全闭标记。
 */
class ParadoxTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (!isAvailable(file)) return Result.CONTINUE
        charTypedInExpression(c, project, editor, file)
        return Result.CONTINUE
    }

    private fun isAvailable(file: PsiFile): Boolean {
        return ParadoxPsiFileMatchService.isScriptFile(file) || ParadoxPsiFileMatchService.isLocalisationFile(file)
    }

    private fun charTypedInExpression(c: Char, project: Project, editor: Editor, file: PsiFile): Boolean {
        charTypedInComplexExpression(c, project, editor, file).let { if (!it) return false }
        charTypedInQuotedStringExpression(c, project, editor, file).let { if (!it) return false }
        return true
    }

    private fun charTypedInComplexExpression(c: Char, project: Project, editor: Editor, file: PsiFile): Boolean {
        val leftMarker = c
        val rightMarker = ParadoxMarkerManager.getMatchedMarkerFromLeft(leftMarker) ?: return true

        val caretOffset = editor.caretModel.offset
        val element = ParadoxPsiFileService.findExpressionForComplexExpression(file, caretOffset, fromToken = true)
        if (element == null) return true

        val gameType = selectGameType(file) ?: return true
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        val complexExpression = ParadoxComplexExpression.resolve(element, configGroup)
        if (complexExpression == null) return true

        // 这里字符尚未输入，当前无法判断要输入的字符是否是开标记，因此直接按复杂表达式类型过滤即可
        if (!ParadoxMarkerManager.isLeftMaker(leftMarker, complexExpression)) return false

        // // 判断刚键入的字符是否被识别为 MarkerNode，且作为开标记
        // val elementOffset = element.startOffset
        // val startOffset = elementOffset + ParadoxExpressionManager.getExpressionOffset(element)
        // val offsetInExpression = caretOffset - startOffset
        // var matched = false
        // complexExpression.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
        //     override fun visit(node: ParadoxComplexExpressionNode): Boolean {
        //         if (node !is ParadoxMarkerNode || node.text.length != 1 || node.text[0] != c) return super.visit(node)
        //         // 刚输入的字符应当位于该 MarkerNode 的末尾（endOffset == caret 相对偏移）
        //         if (node.rangeInExpression.endOffset != offsetInExpression) return super.visit(node)
        //         // 仅当其为开标记时插入闭标记
        //         matched = true
        //         return false
        //     }
        // })
        // if (!matched) return null

        // 如果当前字符已是目标闭标记，且前一个字符不是目标开标记，则不重复插入
        val seq = editor.document.charsSequence
        if ((caretOffset < seq.length && seq[caretOffset] == rightMarker) && (caretOffset > 0 && seq[caretOffset - 1] != leftMarker)) return false

        EditorModificationUtil.insertStringAtCaret(editor, rightMarker.toString(), false, true, 0)
        return false
    }

    @Suppress("UNUSED_PARAMETER")
    private fun charTypedInQuotedStringExpression(c: Char, project: Project, editor: Editor, file: PsiFile): Boolean {
        // #351 make compatible with quoted string expressions in script files, even if it's not a complex expression on semantic level

        val leftMarker = c
        val rightMarker = ParadoxMarkerManager.getMatchedMarkerFromLeft(leftMarker) ?: return true
        val caretOffset = editor.caretModel.offset
        val element = ParadoxPsiFileService.findScriptExpression(file, caretOffset, fromToken = true)
        if (element == null) return true

        val elementText = element.text
        if (!elementText.isLeftQuoted() || !elementText.isRightQuoted()) return true // check double side quotes here

        // 如果当前字符已是目标闭标记，且前一个字符不是目标开标记，则不重复插入
        val seq = editor.document.charsSequence
        if ((caretOffset < seq.length && seq[caretOffset] == rightMarker) && (caretOffset > 0 && seq[caretOffset - 1] != leftMarker)) return false

        EditorModificationUtil.insertStringAtCaret(editor, rightMarker.toString(), false, true, 0)
        return false
    }
}
