package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.ep.inspections.ParadoxIncorrectSyntaxChecker
import icu.windea.pls.lang.inspections.ParadoxInspectionService
import icu.windea.pls.lang.inspections.ParadoxSyntaxInspectionService

/**
 * （本地化文件中的）不正确的语法的代码检查。
 *
 * 可能检测于游戏类型级别、文法级别或语义级别。
 *
 * @see ParadoxIncorrectSyntaxChecker
 */
class IncorrectSyntaxInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val context = ParadoxSyntaxInspectionService.createContext(holder)
        val checkers = ParadoxIncorrectSyntaxChecker.EP_NAME.extensionList
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                ParadoxInspectionService.checkIncorrectSyntax(element, context, checkers)
            }
        }
    }
}
