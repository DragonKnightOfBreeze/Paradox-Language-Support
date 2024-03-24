package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 不正确的脚本语法的检查。
 */
class IncorrectScriptSyntaxInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            ProgressManager.checkCanceled()
            checkComparisonOperator(element)
            checkQuestionEqualSign(element)
        }
        
        private fun checkComparisonOperator(element: PsiElement) {
            //不期望的比较操作符（比较操作符的左值或者右值必须能表示一个数字）
            if(element !is ParadoxScriptProperty) return
            val token = element.findChild(ParadoxScriptTokenSets.COMPARISON_TOKENS) ?: return
            val propertyKey = element.propertyKey
            if(canResolveToNumber(propertyKey)) return
            val propertyValue = element.propertyValue ?: return
            if(canResolveToNumber(propertyValue)) return
            val message = PlsBundle.message("inspection.script.incorrectScriptSyntax.description.1")
            holder.registerProblem(token, message, ProblemHighlightType.GENERIC_ERROR)
        }
        
        @Suppress("UNUSED_PARAMETER")
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
        
        private fun checkQuestionEqualSign(element: PsiElement) {
            //不支持的比较操作符（特指`?=`，仅VIC3支持）
            //https://github.com/cwtools/cwtools/issues/53
            if(element.elementType != ParadoxScriptElementTypes.QUESTION_EQUAL_SIGN) return
            val gameType = selectGameType(element) ?: return
            if(gameType == ParadoxGameType.Vic3) return
            val message = PlsBundle.message("inspection.script.incorrectScriptSyntax.description.2")
            holder.registerProblem(element, message, ProblemHighlightType.GENERIC_ERROR)
        }
    }
}
