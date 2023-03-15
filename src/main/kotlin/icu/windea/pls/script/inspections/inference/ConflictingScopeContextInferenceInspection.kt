package icu.windea.pls.script.inspections.inference

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.scope.*
import icu.windea.pls.script.psi.*

/**
 * 检查作用域上下文的推断结果是否存在冲突。默认不启用。
 */
class ConflictingScopeContextInferenceInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when {
                    element is ParadoxScriptDefinitionElement -> {
                        val definitionInfo = element.definitionInfo
                        if(definitionInfo != null) visitDefinition(element, definitionInfo)
                    }
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