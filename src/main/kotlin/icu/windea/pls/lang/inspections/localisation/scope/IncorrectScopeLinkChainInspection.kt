package icu.windea.pls.lang.inspections.localisation.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

class IncorrectScopeLinkChainInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        return ParadoxFileManager.inLocalisationPath(fileInfo.path)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationExpressionElement) visitExpressionElement(element)
            }

            private fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {
                if (!element.isCommandExpression()) return
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                val commandExpression = ParadoxCommandExpression.resolve(value, textRange, configGroup)
                if (commandExpression == null) return
                checkExpression(element, commandExpression)
            }

            fun checkExpression(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression) {
                doCheckExpression(element, complexExpression)
            }

            private fun doCheckExpression(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression) {
                val scopeNodes = complexExpression.nodes.filterIsInstance<ParadoxCommandScopeLinkNode>()
                val max = ParadoxScopeManager.maxScopeLinkSize
                val actual = scopeNodes.size
                if (actual <= max) return
                val offset = ParadoxExpressionManager.getExpressionOffset(element)
                val startOffset = offset + scopeNodes.first().rangeInExpression.startOffset
                val endOffset = offset + scopeNodes.last().rangeInExpression.endOffset
                val range = TextRange.create(startOffset, endOffset)
                val description = PlsBundle.message("inspection.localisation.incorrectScopeLinkChain.desc.1", max, actual)
                holder.registerProblem(element, range, description)
            }
        }
    }
}
