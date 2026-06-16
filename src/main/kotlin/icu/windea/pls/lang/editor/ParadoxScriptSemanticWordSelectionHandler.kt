package icu.windea.pls.lang.editor

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionWordSelectionRecursiveVisitor
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.editor.ParadoxScriptWordSelectionHandler
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression

/**
 * @see ParadoxScriptWordSelectionHandler
 */
class ParadoxScriptSemanticWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !is ParadoxScriptLanguage) return false
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

    private fun findExpressionElement(element: PsiElement): ParadoxScriptStringExpressionElement? {
        return element.parent?.castOrNull()
    }

    private fun selectExpressionElement(e: PsiElement, cursorOffset: Int, result: MutableList<TextRange>) {
        val element = findExpressionElement(e) ?: return
        val textRange = element.textRange
        if (textRange.isEmpty) return
        selectInComplexExpression(element, cursorOffset, textRange, result)
    }

    private fun selectInComplexExpression(element: ParadoxScriptStringExpressionElement, offset: Int, textRange: TextRange, result: MutableList<TextRange>) {
        // 2.1.10 如果当前光标位于一个复杂表达式中，按照复杂表达式的结构来展开光标

        ProgressManager.checkCanceled()
        if (!element.isExpression()) return
        val file = element.containingFile ?: return
        val expressionText = ParadoxExpressionManager.getExpressionText(element)
        if (expressionText.isEmpty()) return
        val configGroup = PlsFacade.getConfigGroup(file.project, selectGameType(file))
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return
        val complexExpression = ParadoxComplexExpression.resolveByConfig(expressionText, null, configGroup, config) ?: return

        val expressionOffset = ParadoxExpressionManager.getExpressionOffset(element)
        val offsetInExpression = offset - textRange.startOffset - expressionOffset
        val selections = mutableSetOf<TextRange>()
        complexExpression.accept(object : ParadoxComplexExpressionWordSelectionRecursiveVisitor(offsetInExpression) {
            override fun visitWordSelection(node: ParadoxComplexExpressionNode, rangeInExpression: TextRange): Boolean {
                selections += rangeInExpression.shiftRight(textRange.startOffset + expressionOffset)
                return true
            }
        })
        result += selections
    }
}
