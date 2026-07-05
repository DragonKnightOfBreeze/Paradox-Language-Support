package icu.windea.pls.lang.inspections.script.statement

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.parentOfType
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.manipulation.ParadoxConditionalStatementManipulationService
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 建议将条件化语句转换为属性形式。
 *
 * 检测于文法级别。
 *
 * @see ParadoxConditionalStatementManipulationService
 */
class ConditionalStatementToPropertyFormInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitConditionalBlock(element: ParadoxScriptConditionalBlock) {
                ProgressManager.checkCanceled()
                if (!ParadoxConditionalStatementManipulationService.canConvertToPropertyForm(element)) return
                val description = ChronicleBundle.message("inspection.script.conditionalStatementToPropertyForm.desc")
                holder.registerProblem(element, description, Fix())
            }
        }
    }

    private class Fix : PsiUpdateModCommandQuickFix() {
        override fun getFamilyName() = ChronicleBundle.message("inspection.script.conditionalStatementToPropertyForm.fix.1.name")

        override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
            val element = element.parentOfType<ParadoxScriptConditionalBlock>(withSelf = true) ?: return
            ParadoxConditionalStatementManipulationService.convertToPropertyForm(element, project)
        }
    }
}
