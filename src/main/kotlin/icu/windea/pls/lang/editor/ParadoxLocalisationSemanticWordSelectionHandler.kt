package icu.windea.pls.lang.editor

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionRecursiveVisitor
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

class ParadoxLocalisationSemanticWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !is ParadoxLocalisationLanguage) return false
        val element = findExpressionElement(e)
        if (element != null) return true
        return false
    }

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange>? {
        val result = mutableListOf<TextRange>()
        selectExpressionElement(e, cursorOffset, result)
        if (result.isEmpty()) return null
        return result
    }

    private fun findExpressionElement(element: PsiElement): ParadoxLocalisationExpressionElement? {
        return element.parent?.castOrNull()
    }

    private fun selectExpressionElement(e: PsiElement, cursorOffset: Int, result: MutableList<TextRange>) {
        val element = findExpressionElement(e) ?: return
        val textRange = element.textRange
        if (textRange.isEmpty) return
        selectInComplexExpression(element, cursorOffset, textRange, result)
    }

    private fun selectInComplexExpression(element: ParadoxLocalisationExpressionElement, offset: Int, textRange: TextRange, result: MutableList<TextRange>) {
        // 2.1.10 如果当前光标位于一个复杂表达式中，按照复杂表达式的结构来展开光标

        ProgressManager.checkCanceled()
        val file = element.containingFile ?: return
        val expressionText = ParadoxExpressionManager.getExpressionText(element)
        if (expressionText.isEmpty()) return
        val configGroup = PlsFacade.getConfigGroup(file.project, selectGameType(file))
        val complexExpression = ParadoxComplexExpression.resolve(element, configGroup) ?: return

        val expressionOffset = ParadoxExpressionManager.getExpressionOffset(element)
        val offsetInExpression = offset - textRange.startOffset - expressionOffset
        complexExpression.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visitFinished(node: ParadoxComplexExpressionNode): Boolean {
                if (node.nodes.isEmpty() && isCurrentNode(node)) {
                    // 加入当前叶子节点（startOffset <= offset < endOffset）
                    result += node.rangeInExpression.shiftRight(offsetInExpression)
                } else {
                    // 首先加入内层的当前节点（startOffset <= offset < endOffset）
                    val currentNode = node.nodes.find { isCurrentNode(it) } ?: return true
                    result += currentNode.rangeInExpression.shiftRight(offsetInExpression)
                    if (node is ParadoxLinkedExpression && node.rangeInExpression.startOffset != currentNode.rangeInExpression.startOffset) {
                        // 链式表达式开始 ~ 当前链接节点结束
                        val linkedRange = TextRange.create(node.rangeInExpression.startOffset, currentNode.rangeInExpression.endOffset)
                        result += linkedRange.shiftRight(offsetInExpression)
                    }
                }
                return true
            }

            private fun isCurrentNode(node: ParadoxComplexExpressionNode): Boolean {
                if (node !is ParadoxMarkerNode && node !is ParadoxOperatorNode) return offsetInExpression in node.rangeInExpression
                return offsetInExpression >= node.rangeInExpression.startOffset && offsetInExpression <= node.rangeInExpression.endOffset
            }
        })
    }
}
