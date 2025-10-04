package icu.windea.pls.lang.codeInsight.highlighting

import com.intellij.codeInsight.highlighting.HeavyBraceHighlighter
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionUtil
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionVisitor
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 用于在脚本文件和本地化文件中提供基于上下文的语义高亮。
 *
 * - 当光标位置是复杂表达式中成对的标记节点（[ParadoxMarkerNode]）时，高亮这对标记节点。
 */
class ParadoxHeavyBraceHighlighter : HeavyBraceHighlighter() {
    override fun matchBrace(file: PsiFile, offset: Int): Pair<TextRange, TextRange>? {
        if (file !is ParadoxScriptFile && file !is ParadoxLocalisationFile) return null
        if (file.fileInfo == null) return null
        matchBraceInComplexExpression(offset, file)?.let { return it }
        return null
    }

    private fun matchBraceInComplexExpression(offset: Int, file: PsiFile): Pair<TextRange, TextRange>? {
        val caretOffset = offset
        val element = ParadoxPsiFinder.findExpressionForComplexExpression(file, caretOffset, fromToken = true)
        if (element == null) return null

        val project = file.project
        val gameType = selectGameType(file) ?: return null
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val complexExpression = ParadoxComplexExpression.resolve(element, configGroup)
        if (complexExpression == null) return null

        val ref = Ref<Pair<TextRange, TextRange>>()
        val startOffset = element.startOffset + ParadoxExpressionManager.getExpressionOffset(element)
        val offsetInExpression = caretOffset - startOffset
        complexExpression.accept(object : ParadoxComplexExpressionVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (!inRange(node)) return super.visit(node)
                val matched = ParadoxComplexExpressionUtil.getMatchedMarkerNode(node) ?: return super.visit(node)
                ref.set(Pair(node.rangeInExpression.shiftRight(startOffset), matched.rangeInExpression.shiftRight(startOffset)))
                return false
            }

            private fun inRange(node: ParadoxComplexExpressionNode): Boolean {
                return offsetInExpression >= node.rangeInExpression.startOffset && offsetInExpression <= node.rangeInExpression.endOffset
            }
        })
        return ref.get()
    }
}

