package icu.windea.pls.lang.codeInsight.highlighting

import com.intellij.codeInsight.highlighting.HeavyBraceHighlighter
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionRecursiveVisitor
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxMarkerManager

/**
 * 用于在脚本文件和本地化文件中提供基于光标位置的语义高亮。
 *
 * - 当光标位置是复杂表达式中成对的标记节点（[ParadoxMarkerNode]）时，高亮这对标记节点。
 */
class ParadoxHeavyBraceHighlighter : HeavyBraceHighlighter() {
    override fun matchBrace(file: PsiFile, offset: Int): Pair<TextRange, TextRange>? {
        if (!isAvailable(file)) return null
        val ref = Ref<Pair<TextRange, TextRange>>()
        matchBraceInExpression(file, offset, ref)
        return ref.get()
    }

    private fun isAvailable(file: PsiFile): Boolean {
        return ParadoxPsiFileMatchService.isScriptFile(file) || ParadoxPsiFileMatchService.isLocalisationFile(file)
    }

    private fun matchBraceInExpression(file: PsiFile, offset: Int, ref: Ref<Pair<TextRange, TextRange>>): Boolean {
        matchBraceInComplexExpression(file, offset, ref).let { if (!it) return false }
        matchBraceInQuotedStringExpression(file, offset, ref).let { if (!it) return false }
        return true
    }

    private fun matchBraceInComplexExpression(file: PsiFile, offset: Int, ref: Ref<Pair<TextRange, TextRange>>): Boolean {
        val caretOffset = offset
        val element = ParadoxPsiFileService.findExpressionForComplexExpression(file, caretOffset, fromToken = true)
        if (element == null) return true

        val elementText = element.text
        val elementOffset = element.startOffset

        // 预先过滤
        val offsetInElement = caretOffset - elementOffset
        val c1 = elementText.getOrNull(offsetInElement - 1)
        val c2 = elementText.getOrNull(offsetInElement)
        val marker = c1?.takeIf { ParadoxMarkerManager.isLeftOrRightMaker(it) }
            ?: c2?.takeIf { ParadoxMarkerManager.isLeftOrRightMaker(it) }
        if (marker == null) return true

        val project = file.project
        val gameType = selectGameType(file) ?: return true
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        val complexExpression = ParadoxComplexExpression.resolve(element, configGroup)
        if (complexExpression == null) return true

        val startOffset = elementOffset + ParadoxExpressionManager.getExpressionOffset(element)
        val offsetInExpression = caretOffset - startOffset
        complexExpression.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (!inRange(node)) return super.visit(node)
                val matched = ParadoxMarkerManager.getMatchedMarkerNode(node) ?: return super.visit(node)
                ref.set(Pair(node.rangeInExpression.shiftRight(startOffset), matched.rangeInExpression.shiftRight(startOffset)))
                return false
            }

            private fun inRange(node: ParadoxComplexExpressionNode): Boolean {
                return offsetInExpression >= node.rangeInExpression.startOffset && offsetInExpression <= node.rangeInExpression.endOffset
            }
        })
        return false
    }

    private fun matchBraceInQuotedStringExpression(file: PsiFile, offset: Int, ref: Ref<Pair<TextRange, TextRange>>): Boolean {
        // #351 make compatible with quoted string expressions in script files, even if it's not a complex expression on semantic level

        val caretOffset = offset
        val element = ParadoxPsiFileService.findScriptExpression(file, caretOffset, fromToken = true)
        if (element == null) return true

        val elementText = element.text
        if (!elementText.isLeftQuoted() || !elementText.isRightQuoted()) return true // check double side quotes here
        val elementOffset = element.startOffset

        // 预先过滤
        val offsetInElement = caretOffset - elementOffset
        var markerOffsetInElement = offsetInElement
        val c1 = elementText.getOrNull(offsetInElement - 1)
        val c2 = elementText.getOrNull(offsetInElement)
        val marker = c1?.takeIf { ParadoxMarkerManager.isLeftOrRightMaker(it) }?.also { markerOffsetInElement-- }
            ?: c2?.takeIf { ParadoxMarkerManager.isLeftOrRightMaker(it) }
        if (marker == null) return true

        val matchedMarker = ParadoxMarkerManager.getMatchedMarker(marker) ?: return false
        if (matchedMarker == marker) return false // in case

        var depth = 0
        val isLeftMarker = ParadoxMarkerManager.isLeftMaker(marker)
        if (isLeftMarker) {
            for (i in markerOffsetInElement + 1 until elementText.length) {
                val c = elementText[i]
                when (c) {
                    marker -> depth++
                    matchedMarker -> {
                        if (depth == 0) {
                            ref.set(Pair(TextRange.from(markerOffsetInElement + elementOffset, 1), TextRange.from(i + elementOffset, 1)))
                            break
                        }
                        depth--
                    }
                }
            }
        } else {
            for (i in markerOffsetInElement - 1 downTo 0) {
                val c = elementText[i]
                when (c) {
                    marker -> depth++
                    matchedMarker -> {
                        if (depth == 0) {
                            ref.set(Pair(TextRange.from(markerOffsetInElement + elementOffset, 1), TextRange.from(i + elementOffset, 1)))
                            break
                        }
                        depth--
                    }
                }
            }
        }
        return false
    }
}

