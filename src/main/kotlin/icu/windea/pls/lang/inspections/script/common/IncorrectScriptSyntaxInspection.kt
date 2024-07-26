@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 不正确的脚本语法的检查。
 */
class IncorrectScriptSyntaxInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            ProgressManager.checkCanceled()
            checkComparisonOperator(element)
        }
        
        private fun checkComparisonOperator(element: PsiElement) {
            //不期望的比较操作符（比较操作符的左值或者右值必须能表示一个数字）
            if(element !is ParadoxScriptProperty) return
            val token = element.findChild(ParadoxScriptTokenSets.COMPARISON_TOKENS) ?: return
            val propertyKey = element.propertyKey
            if(canResolveToNumber(propertyKey)) return
            val propertyValue = element.propertyValue ?: return
            if(canResolveToNumber(propertyValue)) return
            val message = PlsBundle.message("inspection.script.incorrectScriptSyntax.desc.1")
            holder.registerProblem(token, message, ProblemHighlightType.GENERIC_ERROR)
        }
        
        private fun canResolveToNumber(element: ParadoxScriptPropertyKey): Boolean {
            //number, scalar, parametric
            return true
        }
        
        private fun canResolveToNumber(element: ParadoxScriptValue): Boolean {
            return when {
                element is ParadoxScriptInt -> true
                element is ParadoxScriptFloat -> true
                element is ParadoxScriptScriptedVariableReference -> {
                    val resolved = element.reference?.resolve() ?: return true 
                    val resolvedValueElement = resolved.scriptedVariableValue ?: return true
                    canResolveToNumber(resolvedValueElement)
                }
                element is ParadoxScriptString -> true //scalar, parametric
                element is ParadoxScriptInlineMath -> true
                else -> false
            }
        }
    }
}
