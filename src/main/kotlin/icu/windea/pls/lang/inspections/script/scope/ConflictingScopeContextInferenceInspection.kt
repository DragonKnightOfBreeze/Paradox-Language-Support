package icu.windea.pls.lang.inspections.script.scope

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.resolve.ParadoxScopeService
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 检查作用域上下文的推断结果是否存在冲突。默认不启用。
 */
class ConflictingScopeContextInferenceInspection : ScopeInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxPsiElementVisitor() {
            override fun visitDefinitionElement(element: ParadoxDefinitionElement) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    private fun check(element: ParadoxDefinitionElement, holder: ProblemsHolder) {
        val definitionInfo = element.definitionInfo ?: return
        val description = ParadoxScopeService.getInferenceErrorMessage(element, definitionInfo)
        if (description == null) return
        val location = if (element is ParadoxScriptProperty) element.propertyKey else element
        holder.registerProblem(location, description)
    }
}
