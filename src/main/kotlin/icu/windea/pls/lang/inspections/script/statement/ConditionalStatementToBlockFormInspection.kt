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
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.manipulation.ParadoxConditionalStatementManipulationService
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 建议将条件化语句转换为块形式。
 *
 * 检测于文法级别。
 *
 * @see ParadoxConditionalStatementManipulationService
 */
class ConditionalStatementToBlockFormInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                if (!ParadoxConditionalStatementManipulationService.canConvertToBlockForm(element)) return
                val description = PlsBundle.message("inspection.script.conditionalStatementToBlockForm.desc")
                val fixes = getFixes(element)
                holder.registerProblem(element.propertyKey, description, *fixes)
            }
        }
    }

    private fun getFixes(element: ParadoxScriptProperty): Array<out LocalQuickFix> {
        return arrayOf(Fix(element))
    }

    private class Fix(
        element: PsiElement
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption {
        override fun getText() = PlsBundle.message("inspection.script.conditionalStatementToBlockForm.fix.1.name")

        override fun getFamilyName() = text

        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            val element = startElement as? ParadoxScriptProperty ?: return
            ParadoxConditionalStatementManipulationService.convertToBlockForm(element, project)
        }
    }
}
