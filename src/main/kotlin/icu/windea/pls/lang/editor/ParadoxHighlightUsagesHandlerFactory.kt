package icu.windea.pls.lang.editor

import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.findParentOfType
import com.intellij.psi.util.startOffset
import com.intellij.util.Consumer
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.findElementAt
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionUtil
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionVisitor
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 用于在脚本文件中提供基于上下文的额外高亮。
 *
 * - 当光标位置是复杂表达式中成对的标记节点（[ParadoxMarkerNode]）时，高亮这对标记节点。
 */
class ParadoxHighlightUsagesHandlerFactory : HighlightUsagesHandlerFactory {
    override fun createHighlightUsagesHandler(editor: Editor, file: PsiFile): HighlightUsagesHandlerBase<*>? {
        val targets = mutableListOf<PsiElement>()
        val matchedBraces = mutableListOf<TextRange>()
        addForMarkerNodes(file, editor, targets, matchedBraces)
        if (targets.isEmpty() || matchedBraces.isEmpty()) return null
        return object : HighlightUsagesHandlerBase<PsiElement>(editor, file) {
            override fun getTargets() = targets

            override fun selectTargets(targets: List<PsiElement>, selectionConsumer: Consumer<in List<PsiElement>>) = selectionConsumer.consume(targets)

            override fun computeUsages(targets: List<PsiElement>) {}

            override fun highlightUsages() {
                highlightMatchedBraces()
                super.highlightUsages()
            }

            private fun highlightMatchedBraces() {
                val highlightManager = HighlightManager.getInstance(myFile.project)
                val clearHighlights = HighlightUsagesHandler.isClearHighlights(myEditor)
                HighlightUsagesHandler.highlightRanges(highlightManager, myEditor, CodeInsightColors.MATCHED_BRACE_ATTRIBUTES, clearHighlights, matchedBraces)
            }
        }
    }

    private fun addForMarkerNodes(file: PsiFile, editor: Editor, targets: MutableList<PsiElement>, occurrences: MutableList<TextRange>) {
        // com.intellij.codeInsight.highlighting.BraceHighlightingHandler
        // com.intellij.openapi.editor.colors.CodeInsightColors.MATCHED_BRACE_ATTRIBUTES

        val caretOffset = editor.caretModel.offset
        val target = file.findElementAt(caretOffset) { e -> e.findParentOfType<ParadoxScriptStringExpressionElement>() }
        if (target == null) return
        targets += target

        val project = file.project
        val gameType = selectGameType(file) ?: return
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val complexExpression = ParadoxComplexExpression.resolve(target, configGroup)
        if (complexExpression == null) return

        val startOffset = target.startOffset + ParadoxExpressionManager.getExpressionOffset(target)
        val offsetInExpression = caretOffset - startOffset
        complexExpression.accept(object : ParadoxComplexExpressionVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (!inRange(node)) return super.visit(node)
                val matched = ParadoxComplexExpressionUtil.getMatchedMarkerNode(node) ?: return super.visit(node)
                occurrences.add(node.rangeInExpression.shiftRight(startOffset))
                occurrences.add(matched.rangeInExpression.shiftRight(startOffset))
                return false
            }

            private fun inRange(node: ParadoxComplexExpressionNode): Boolean {
                return offsetInExpression >= node.rangeInExpression.startOffset && offsetInExpression <= node.rangeInExpression.endOffset
            }
        })
    }
}
