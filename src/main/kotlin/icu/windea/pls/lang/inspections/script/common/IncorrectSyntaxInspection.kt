package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.ep.inspections.ParadoxIncorrectSyntaxChecker
import icu.windea.pls.lang.inspections.ParadoxSyntaxInspectionContext
import icu.windea.pls.lang.inspections.ParadoxSyntaxInspectionService

/**
 * 查是否存在不正确的语法。
 *
 * 可能检测于游戏类型级别、文法级别或语义级别。
 *
 * @see ParadoxIncorrectSyntaxChecker
 */
class IncorrectSyntaxInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val context = createContext(holder)
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                ParadoxSyntaxInspectionService.checkForIncorrectSyntax(element, context)
            }
        }
    }

    private fun createContext(holder: ProblemsHolder): ParadoxSyntaxInspectionContext {
        return ParadoxSyntaxInspectionContext(this, holder)
    }
}
