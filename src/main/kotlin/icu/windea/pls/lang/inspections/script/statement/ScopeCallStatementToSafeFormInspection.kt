package icu.windea.pls.lang.inspections.script.statement

import com.intellij.codeInsight.daemon.impl.actions.IntentionActionWithFixAllOption
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 建议将显式作用域调用转换为安全形式。
 *
 * 说明：
 * - 适用于支持安全（调用）赋值操作符的游戏类型（CK3/VIC3/EU5 使用 `?=`，Stellaris 使用 `? =`）。
 *
 * @see ParadoxScopeCallStatementManipulationService
 */
class ScopeCallStatementToSafeFormInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                if (!ParadoxScopeCallStatementManipulationService.canConvertToSafeForm(element)) return
                val description = PlsBundle.message("inspection.script.scopeCallToSafeForm.desc")
                val fixes = getFixes(element)
                holder.registerProblem(element.propertyKey, description, *fixes)
            }
        }
    }

    private fun getFixes(element: ParadoxScriptProperty): Array<Fix> {
        return arrayOf(Fix(element))
    }

    private class Fix(
        element: PsiElement
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption {
        override fun getText() = PlsBundle.message("inspection.script.scopeCallToSafeForm.fix.1.name")

        override fun getFamilyName() = text

        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            val property = (startElement as? ParadoxScriptProperty) ?: return
            val gameType = ParadoxAnalysisManager.selectGameType(property) ?: ParadoxGameType.getDefault()
            ParadoxScopeCallStatementManipulationService.convertToSafeForm(property, project, gameType)
        }
    }
}
