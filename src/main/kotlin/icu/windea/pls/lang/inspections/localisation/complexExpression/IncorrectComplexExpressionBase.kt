package icu.windea.pls.lang.inspections.localisation.complexExpression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

/**
 * 不正确的复杂表达式的检查的基类。
 */
abstract class IncorrectComplexExpressionBase : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        return ParadoxPsiFileMatcher.isLocalisationFile(file, smart = true, injectable = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationExpressionElement) visitExpressionElement(element)
            }

            private fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {
                val complexExpression = ParadoxComplexExpression.resolve(element, configGroup) ?: return
                val errors = complexExpression.getAllErrors(element)
                if (errors.isEmpty()) return
                val fixes = getFixes(element, complexExpression)
                errors.forEach { error -> error.register(element, holder, *fixes) }
            }
        }
    }

    protected open fun getFixes(element: ParadoxLocalisationExpressionElement, complexExpression: ParadoxComplexExpression): Array<LocalQuickFix> {
        return LocalQuickFix.EMPTY_ARRAY
    }
}
