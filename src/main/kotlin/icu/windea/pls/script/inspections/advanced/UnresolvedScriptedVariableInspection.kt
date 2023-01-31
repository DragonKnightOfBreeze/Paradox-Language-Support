package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.script.psi.*

/**
 * 无法解析的封装变量引用的检查。
 *
 * 提供快速修复：
 * * 声明本地封装变量（在同一文件中）
 * * 声明全局封装变量（在`common/scripted_variables`目录下的某一文件中）
 * * 导入游戏目录或模组目录
 */
class UnresolvedScriptedVariableInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxScriptVisitor() {
		override fun visitScriptedVariableReference(element: ParadoxScriptScriptedVariableReference) {
			doVisitScriptedVariableReference(element)
		}
		
		override fun visitInlineMathScriptedVariableReference(element: ParadoxScriptInlineMathScriptedVariableReference) {
			doVisitScriptedVariableReference(element)
		}
		
		fun doVisitScriptedVariableReference(element: ParadoxScriptedVariableReference) {
			ProgressManager.checkCanceled()
			if(element.reference.canResolve()) return
			val variableName = element.name
			val quickFixes = listOf(
				IntroduceLocalVariableFix(variableName, element),
				IntroduceGlobalVariableFix(variableName, element),
				ImportGameOrModDirectoryFix(element)
			)
			val message = PlsBundle.message("inspection.script.advanced.unresolvedScriptedVariable.description", element.name)
			holder.registerProblem(element, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, *quickFixes.toTypedArray())
		}
	}
	
}

