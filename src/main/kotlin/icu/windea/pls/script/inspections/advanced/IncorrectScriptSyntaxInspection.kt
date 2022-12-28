package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.script.psi.*

/**
 * 不正确的脚本语法的检查。
 *
 * 包括以下几种情况：
 * * 不期望的比较操作符（必须在数字值之前）
 * * TODO 不期望的封装变量引用（必须位于定义之内）
 * * TODO 不期望的参数（必须位于定义之内）
 * * TODO 不期望的参数条件语句（必须位于定义之内）
 * * TODO 不期望的内联数学表达式（必须位于定义之内）
 * * TODO 不期望的封装本地化引用（必须位于定义之内）
 * * TODO 不期望的内联数学表达式的开始（"@["和"@\["） -> 提供快速修复：修正
 * * TODO 不期望的封装本地化引用的开始（"["和"\["） -> 提供快速修复：修正
 * * TODO 同一scripted_effect/scripted_trigger定义中存在多个内联数字表达式
 */
class IncorrectScriptSyntaxInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				if(element is ParadoxScriptProperty) visitProperty(element)
				super.visitElement(element)
			}
			
			private fun visitProperty(element: ParadoxScriptProperty) {
				ProgressManager.checkCanceled()
				val propertyKey = element.propertyKey
				if(mayBeNumberKey(propertyKey)) return
				val propertyValue = element.propertyValue ?: return
				if(mayByNumberValue(propertyValue)) return
				val comparisonTokens = element.findChildren(ParadoxScriptTokenSets.COMPARISONS)
				if(comparisonTokens.isEmpty()) return
				val message = PlsBundle.message("script.inspection.advanced.incorrectScriptSyntax.description.1")
				comparisonTokens.forEach {
					holder.registerProblem(it, message, ProblemHighlightType.GENERIC_ERROR)
				}
			}
			
			//int or float (can be quoted)
			//scripted variable (int or float value)
			//value field or variable field
			//inline math
			
			private fun mayBeNumberKey(element: ParadoxScriptPropertyKey): Boolean {
				if(ParadoxDataType.resolve(element.value).isNumberType()) return true
				val config = ParadoxCwtConfigHandler.resolveConfigs(element, orDefault = false).firstOrNull() ?: return false
				return config.expression.type.isNumberType()
			}
			
			private fun mayByNumberValue(element: ParadoxScriptValue): Boolean {
				//NOTE 后面仍然可能是个枚举值，无法验证！
				return when {
					element is ParadoxScriptInt -> true
					element is ParadoxScriptFloat -> true
					element is ParadoxScriptScriptedVariableReference -> {
						val resolved = element.reference.resolve()
						val resolvedValueElement = resolved?.scriptedVariableValue
						resolvedValueElement == null || mayByNumberValue(resolvedValueElement)
					}
					element is ParadoxScriptString -> {
						if(ParadoxDataType.resolve(element.value).isNumberType()) return true
						val config = ParadoxCwtConfigHandler.resolveConfigs(element, orDefault = false).firstOrNull() ?: return false
						return config.expression.type.isNumberType()
					}
					element is ParadoxScriptInlineMath -> true
					else -> false
				}
			}
		})
		return holder.resultsArray
	}
}
