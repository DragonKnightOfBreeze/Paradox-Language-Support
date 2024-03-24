package icu.windea.pls.lang.inspections.script.inference

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.scope.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 检查作用域上下文的推断结果是否存在冲突。默认不启用。
 */
class ConflictingScopeContextInferenceInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptDefinitionElement) {
                    val definitionInfo = element.definitionInfo
                    if(definitionInfo != null) visitDefinition(element, definitionInfo)
                }
            }
            
            private fun visitDefinition(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
                val message = ParadoxDefinitionInferredScopeContextProvider.getErrorMessage(element, definitionInfo)
                if(message != null) {
                    val location = if(element is ParadoxScriptProperty) element.propertyKey else element
                    holder.registerProblem(location, message)
                }
            }
        }
    }
}