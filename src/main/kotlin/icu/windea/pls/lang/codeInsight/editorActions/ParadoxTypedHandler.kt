package icu.windea.pls.lang.codeInsight.editorActions

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionUtil
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 用于在脚本文件和本地化文件中提供基于当前输入与上下文的代码补全。
 *
 * - 在复杂表达式中，当键入成对标记（marker）的开标记时，自动补全闭标记。
 *   仅当键入位置可被识别为复杂表达式中的 [ParadoxMarkerNode] 且是开标记时才插入闭标记。
 */
class ParadoxTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file !is ParadoxScriptFile && file !is ParadoxLocalisationFile) return Result.CONTINUE
        if (file.fileInfo == null) return Result.CONTINUE
        charTypedInComplexExpression(c, project, editor, file)?.let { return it }
        return Result.CONTINUE
    }

    private fun charTypedInComplexExpression(c: Char, project: Project, editor: Editor, file: PsiFile): Result? {
        val leftMarker = c.toString()
        val closeMarker = ParadoxComplexExpressionUtil.getMatchedMarker(leftMarker) ?: return null
        val closeChar = closeMarker.singleOrNull() ?: return null
        val caretOffset = editor.caretModel.offset
        val element = ParadoxPsiFinder.findExpressionForComplexExpression(file, caretOffset, fromToken = true)
        if (element == null) return null

        val gameType = selectGameType(file) ?: return null
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val complexExpression = ParadoxComplexExpression.resolve(element, configGroup)
        if (complexExpression == null) return null

        // 这里字符尚未输入，当前无法判断要输入的字符是否是开标记，因此直接按复杂表达式类型过滤即可
        if (!ParadoxComplexExpressionUtil.isLeftMaker(leftMarker, complexExpression)) return null

        // // 判断刚键入的字符是否被识别为 MarkerNode，且作为开标记
        // val elementOffset = element.startOffset
        // val startOffset = elementOffset + ParadoxExpressionManager.getExpressionOffset(element)
        // val offsetInExpression = caretOffset - startOffset
        // var matched = false
        // complexExpression.accept(object : ParadoxComplexExpressionVisitor() {
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

        // 若下一字符已是目标闭标记，则不重复插入
        val seq = editor.document.charsSequence
        if (caretOffset < seq.length && seq[caretOffset] == closeChar) return null

        EditorModificationUtil.insertStringAtCaret(editor, closeMarker, false, true, 0)
        return null
    }
}
