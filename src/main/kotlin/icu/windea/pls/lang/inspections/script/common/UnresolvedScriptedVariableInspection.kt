package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.quickfix.*

/**
 * 无法解析的封装变量引用的检查。
 *
 * 提供快速修复：
 * * 声明本地封装变量（在同一文件中）
 * * 声明全局封装变量（在`common/scripted_variables`目录下的某一个文件中）
 * * 导入游戏目录或模组目录
 */
class UnresolvedScriptedVariableInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return object : PsiElementVisitor() {
			override fun visitElement(element: PsiElement) {
				ProgressManager.checkCanceled()
				if(element is ParadoxScriptedVariableReference) visitScriptedVariableReference(element)
			}
			
			private fun visitScriptedVariableReference(element: ParadoxScriptedVariableReference) {
				val name = element.name ?: return
				if(name.isParameterized()) return //skip if name is parameterized
				val reference = element.reference ?: return
				if(reference.resolve() != null) return
				val quickFixes = listOf(
					IntroduceLocalVariableFix(name, element),
					IntroduceGlobalVariableFix(name, element)
				)
				val message = PlsBundle.message("inspection.script.unresolvedScriptedVariable.description", name)
				holder.registerProblem(element, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, *quickFixes.toTypedArray())
			}
		}
	}
}

