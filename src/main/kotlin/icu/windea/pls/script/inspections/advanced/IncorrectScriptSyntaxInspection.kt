package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

/**
 * 不正确的脚本语法的检查。
 *
 * 包括以下几种情况：
 * * 不期望的比较操作符（必须在数字值之前）
 * * TODO 不期望的封装变量引用（必须位于定义之内）
 * * TODO 不期望的参数（必须位于定义之内）
 * * TODO 不期望的参数条件语句（必须位于定义之内）
 * * TODO 不期望的内联表达式（必须位于定义之内）
 * * TODO 不期望的封装本地化引用（必须位于定义之内）
 * * TODO 不期望的内联数学表达式的开始（"@["和"@\["） -> 提供快速修复：修正
 * * TODO 不期望的封装本地化引用的开始（"["和"\["） -> 提供快速修复：修正
 * * TODO 同一scripted_effect/scripted_trigger定义中存在多个内联数字表达式
 */
class IncorrectScriptSyntaxInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxScriptVisitor() {
		override fun visitPropertyValue(e: ParadoxScriptPropertyValue) {
			//检查：不期望的比较操作符
			val valueElement = e.value
			if(!mayByNumberValue(valueElement)) {
				valueElement.siblings(forward = false, withSelf = false).forEach {
					if(isOperator(it)) {
						val message = PlsBundle.message("script.inspection.advanced.incorrectScriptSyntax.description.1")
						holder.registerProblem(it, message, ProblemHighlightType.GENERIC_ERROR)
					}
				}
			}
		}
		
		private fun mayByNumberValue(element: ParadoxScriptValue): Boolean {
			return when {
				element is ParadoxScriptInt -> true
				element is ParadoxScriptFloat -> true
				element is ParadoxScriptScriptedVariableReference -> {
					val resolved = element.reference.resolve()
					val resolvedValueElement = resolved?.scriptedVariableValue?.value
					resolvedValueElement == null || mayByNumberValue(resolvedValueElement)
				}
				element is ParadoxScriptInlineMath -> true
				else -> false
			}
		}
		
		private fun isOperator(element: PsiElement): Boolean {
			//LT_SIGN | GT_SIGN | LE_SIGN | GE_SIGN | NOT_EQUAL_SIGN 
			val elementType = element.elementType
			return elementType == LT_SIGN || elementType == GT_SIGN || elementType == LE_SIGN || elementType == GE_SIGN
				|| elementType == NOT_EQUAL_SIGN
		}
	}
}