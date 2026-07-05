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
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 建议将作用域调用转换为嵌套形式。
 *
 * 检测于文法级别和语义级别。
 *
 * @see ParadoxScopeCallStatementManipulationService
 */
class ScopeCallStatementToNestedFormInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val gameType = selectGameType(holder.file)
        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                if (!ParadoxScopeCallStatementManipulationService.canConvertToNestedForm(element, gameType)) return
                val range = ParadoxScopeCallStatementManipulationService.getHighlightingRange(element)
                val description = ChronicleBundle.message("inspection.script.scopeCallStatementToNestedForm.desc")
                holder.registerProblem(element, range, description, Fix(gameType))
            }
        }
    }

    private class Fix(private val gameType: ParadoxGameType?) : PsiUpdateModCommandQuickFix() {
        override fun getFamilyName() = ChronicleBundle.message("inspection.script.scopeCallStatementToNestedForm.fix.1.name")

        override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
            val element = element.parentOfType<ParadoxScriptProperty>(withSelf = true) ?: return
            val moveTo = ParadoxScopeCallStatementManipulationService.convertToNestedForm(element, project, updater.caretOffset, gameType)
            if (moveTo >= 0) updater.moveCaretTo(moveTo)
        }
    }
}
