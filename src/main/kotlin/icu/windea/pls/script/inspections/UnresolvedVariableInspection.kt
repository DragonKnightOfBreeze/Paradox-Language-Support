package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

private fun _description(name: String) = PlsBundle.message("script.inspection.unresolvedVariable.description", name)
private fun _quickFix1Name(name: String) = PlsBundle.message("script.inspection.unresolvedVariable.quickFix.1", name)
private fun _quickFix2Name(name: String) = PlsBundle.message("script.inspection.unresolvedVariable.quickFix.2", name)

/**
 * 无法解析的变量引用的检查。
 *
 * 提供快速修复：
 * * TODO 声明本地变量（在同一文件中）
 * * TODO 声明全局变量（在`common/script_variables`目录下的某一文件中）
 * * TODO 导入游戏目录或模组目录
 */
class UnresolvedVariableInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
		return Visitor(holder, session)
	}
	
	private class Visitor(private val holder: ProblemsHolder, private val session: LocalInspectionToolSession) : ParadoxScriptVisitor() {
		override fun visitVariableReference(element: ParadoxScriptVariableReference) {
			val reference = element.reference
			if(reference.resolve() != null) return
			val location = element.variableReferenceId
			holder.registerProblem(location, _description(element.name),
				//IntroduceLocalVariable(variableName, element, session),
				//IntroduceGlobalVariable(variableName, element, session),
				//ImportGameOrModDirectory(element)
			)
		}
	}
	
	private class IntroduceLocalVariable(
		private val variableName: String,
		element: ParadoxScriptVariableReference,
		session: LocalInspectionToolSession
	) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
		override fun getFamilyName() = _quickFix1Name(variableName)
		
		override fun getText() = _quickFix1Name(variableName)
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			//TODO
		}
	}
	
	private class IntroduceGlobalVariable(
		private val variableName: String,
		element: ParadoxScriptVariableReference,
		session: LocalInspectionToolSession
	) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
		override fun getFamilyName() = _quickFix2Name(variableName)
		
		override fun getText() = _quickFix2Name(variableName)
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			//TODO
		}
	}
}

