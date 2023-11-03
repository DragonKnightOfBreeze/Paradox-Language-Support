package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.script.psi.*

/**
 * 不正确的脚本语法的检查。
 *
 * 包括以下几种情况：
 * * 不期望的比较操作符（必须在表示数字的值之前或之后，但不一定需要写作数字，也可能是字符串）
 */
class IncorrectScriptSyntaxInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            if(element is ParadoxScriptProperty) visitProperty(element)
        }
        
        private fun visitProperty(element: ParadoxScriptProperty) {
            ProgressManager.checkCanceled()
            val comparisonToken = element.findChild(ParadoxScriptTokenSets.COMPARISON_TOKENS) ?: return
            val propertyKey = element.propertyKey
            if(mayKeyRepresentNumber(propertyKey)) return
            val propertyValue = element.propertyValue ?: return
            if(mayValueRepresentNumber(propertyValue)) return
            val message = PlsBundle.message("inspection.script.general.incorrectScriptSyntax.description.1")
            holder.registerProblem(comparisonToken, message, ProblemHighlightType.GENERIC_ERROR)
        }
        
        private fun mayKeyRepresentNumber(element: ParadoxScriptPropertyKey): Boolean {
            //number, scalar, parametric
            return true
        }
        
        private fun mayValueRepresentNumber(element: ParadoxScriptValue): Boolean {
            return when {
                element is ParadoxScriptInt -> true
                element is ParadoxScriptFloat -> true
                element is ParadoxScriptScriptedVariableReference -> {
                    val resolved = element.reference?.resolve() ?: return true 
                    val resolvedValueElement = resolved.scriptedVariableValue ?: return true
                    mayValueRepresentNumber(resolvedValueElement)
                }
                element is ParadoxScriptString -> true //scalar, parametric
                element is ParadoxScriptInlineMath -> true
                else -> false
            }
        }
    }
}
