package icu.windea.pls.lang.inspections.script.inference

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ep.scope.ParadoxDefinitionInferredScopeContextProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 检查作用域上下文的推断结果是否存在冲突。默认不启用。
 */
class ConflictingScopeContextInferenceInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptDefinitionElement) {
                    val definitionInfo = element.definitionInfo
                    if (definitionInfo != null) visitDefinition(element, definitionInfo)
                }
            }

            private fun visitDefinition(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
                val message = ParadoxDefinitionInferredScopeContextProvider.getErrorMessage(element, definitionInfo)
                if (message != null) {
                    val location = if (element is ParadoxScriptProperty) element.propertyKey else element
                    holder.registerProblem(location, message)
                }
            }
        }
    }
}
