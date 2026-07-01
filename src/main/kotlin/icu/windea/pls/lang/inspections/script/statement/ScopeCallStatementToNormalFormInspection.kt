package icu.windea.pls.lang.inspections.script.statement

import com.intellij.codeInsight.daemon.impl.actions.IntentionActionWithFixAllOption
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 建议将作用域调用转换为普通形式。
 *
 * 检测于文法级别和语义级别。
 *
 * 适用于所有游戏类型和任意安全调用运算符（`?=` 或 `? =`）。
 *
 * @see ParadoxScopeCallStatementManipulationService
 */
class ScopeCallStatementToNormalFormInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                if (!ParadoxScopeCallStatementManipulationService.canConvertToNormalForm(element)) return
                val description = ChronicleBundle.message("inspection.script.scopeCallStatementToNormalForm.desc")
                val fixes = getFixes(element)
                holder.registerProblem(element.propertyKey, description, *fixes)
            }
        }
    }

    private fun getFixes(element: ParadoxScriptProperty): Array<LocalQuickFix> {
        return arrayOf(Fix(element))
    }

    private class Fix(
        element: PsiElement
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption {
        override fun getText() = ChronicleBundle.message("inspection.script.scopeCallStatementToNormalForm.fix.1.name")

        override fun getFamilyName() = text

        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            val element = startElement as? ParadoxScriptProperty ?: return
            ParadoxScopeCallStatementManipulationService.convertToNormalForm(element, project)
        }
    }
}
