package icu.windea.pls.script.inspections.inference

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.provider.*
import icu.windea.pls.script.psi.*

/**
 * 检查作用域上下文的推断结果是否存在冲突。
 */
class ConflictingScopeContextInferenceInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object :PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when {
                    element is ParadoxScriptProperty && element.definitionInfo != null -> visitDefinition(element)
                }
            }
    
            private fun visitDefinition(element: ParadoxScriptProperty) {
                val message = ParadoxInferredScopeContextProvider.getErrorMessageForDefinition(element)
                if(message != null) {
                    holder.registerProblem(element, message)
                }
            }
        }
    }
}