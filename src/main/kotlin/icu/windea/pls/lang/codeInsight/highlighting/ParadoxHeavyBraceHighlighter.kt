package icu.windea.pls.lang.codeInsight.highlighting

import com.intellij.codeInsight.highlighting.HeavyBraceHighlighter
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionRecursiveVisitor
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionUtil
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager

/**
 * 用于在脚本文件和本地化文件中提供基于上下文的语义高亮。
 *
 * - 当光标位置是复杂表达式中成对的标记节点（[ParadoxMarkerNode]）时，高亮这对标记节点。
 */
class ParadoxHeavyBraceHighlighter : HeavyBraceHighlighter() {
    override fun matchBrace(file: PsiFile, offset: Int): Pair<TextRange, TextRange>? {
        val matched = ParadoxPsiFileMatcher.isScriptFile(file, injectable = true) || ParadoxPsiFileMatcher.isLocalisationFile(file, injectable = true)
        if (!matched) return null
        matchBraceInComplexExpression(offset, file)?.let { return it }
        return null
    }

    private fun matchBraceInComplexExpression(offset: Int, file: PsiFile): Pair<TextRange, TextRange>? {
        val caretOffset = offset
        val element = ParadoxPsiFileManager.findExpressionForComplexExpression(file, caretOffset, fromToken = true)
        if (element == null) return null

        val elementOffset = element.startOffset
        val startOffset = elementOffset + ParadoxExpressionManager.getExpressionOffset(element)
        val offsetInExpression = caretOffset - startOffset

        // 预先过滤
        val text = element.text
        val c1 = text.getOrNull(caretOffset - elementOffset - 1)
        val c2 = text.getOrNull(caretOffset - elementOffset)
        val possible = (c1 != null && (ParadoxComplexExpressionUtil.isLeftOrRightMaker(c1.toString())))
            || (c2 != null && ParadoxComplexExpressionUtil.isLeftOrRightMaker(c2.toString()))
        if (!possible) return null

        val project = file.project
        val gameType = selectGameType(file) ?: return null
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val complexExpression = ParadoxComplexExpression.resolve(element, configGroup)
        if (complexExpression == null) return null

        val ref = Ref<Pair<TextRange, TextRange>>()
        complexExpression.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
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

