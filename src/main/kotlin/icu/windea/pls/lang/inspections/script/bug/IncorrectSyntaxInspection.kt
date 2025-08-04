package icu.windea.pls.lang.inspections.script.bug

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 不正确的语法的检查。
 */
class IncorrectSyntaxInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                checkComparisonOperator(element)
            }

            private fun checkComparisonOperator(element: PsiElement) {
                //不期望的比较操作符（比较操作符的左值或者右值必须能表示一个数字）
                if (element !is ParadoxScriptProperty) return
                val token = element.findChild { it.elementType in ParadoxScriptTokenSets.COMPARISON_TOKENS } ?: return
                val propertyKey = element.propertyKey
                if (canResolveToNumber(propertyKey)) return
                val propertyValue = element.propertyValue ?: return
                if (canResolveToNumber(propertyValue)) return
                val message = PlsBundle.message("inspection.script.incorrectSyntax.desc.1")
                holder.registerProblem(token, message, ProblemHighlightType.GENERIC_ERROR)
            }

            @Suppress("unused")
            private fun canResolveToNumber(element: ParadoxScriptPropertyKey): Boolean {
                //number, scalar, parametric
                return true
            }

            private fun canResolveToNumber(element: ParadoxScriptValue): Boolean {
                return when {
                    element is ParadoxScriptInt -> true
                    element is ParadoxScriptFloat -> true
                    element is ParadoxScriptScriptedVariableReference -> {
                        val resolved = element.resolved() ?: return true
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
}
