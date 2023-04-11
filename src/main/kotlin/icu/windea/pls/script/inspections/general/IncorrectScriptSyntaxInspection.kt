package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 不正确的脚本语法的检查。
 *
 * 包括以下几种情况：
 * * 不期望的比较操作符（必须在**表示**数字的值之前，但不一定需要写作数字，也可能是字符串）
 * * TODO 不期望的封装变量引用（必须位于定义声明之内）
 * * TODO 不期望的参数（必须位于定义声明之内）
 * * TODO 不期望的参数条件语句（必须位于定义声明之内）
 * * TODO 不期望的内联数学表达式（必须位于定义声明之内）
 * * TODO 不期望的封装本地化引用（必须位于定义声明之内）
 * * TODO 不期望的内联数学表达式的开始（"@["和"@\["） > 提供快速修复：修正
 * * TODO 不期望的封装本地化引用的开始（"["和"\["） > 提供快速修复：修正
 * * TODO 同一scripted_effect/scripted_trigger定义中存在多个内联数字表达式
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
                    val resolved = element.reference.resolve()
                    val resolvedValueElement = resolved?.scriptedVariableValue
                    resolvedValueElement == null || mayValueRepresentNumber(resolvedValueElement)
                }
                element is ParadoxScriptString -> true //scalar, parametric
                element is ParadoxScriptInlineMath -> true
                else -> false
            }
        }
    }
}
