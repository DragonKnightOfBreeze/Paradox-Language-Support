package icu.windea.pls.lang.inspections.localisation.scope

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.resolving.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolving.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolving.complexExpression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isCommandExpression
import icu.windea.pls.model.paths.ParadoxPathMatcher
import icu.windea.pls.model.paths.matches

class IncorrectScopeLinkChainInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        return fileInfo.path.matches(ParadoxPathMatcher.InLocalisationPath)
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
