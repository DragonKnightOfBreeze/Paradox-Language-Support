package icu.windea.pls.script.inspections

import com.intellij.codeInsight.intention.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.quickFix.*
import icu.windea.pls.script.psi.*

private const val i = 1

/**
 * 无法解析的封装变量引用的检查。
 *
 * 提供快速修复：
 * * 声明本地封装变量（在同一文件中）
 * * TODO 声明全局封装变量（在`common/scripted_variables`目录下的某一文件中）
 * * 导入游戏目录或模组目录
 */
class UnresolvedScriptedVariableInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxScriptVisitor() {
		override fun visitVariableReference(element: ParadoxScriptVariableReference) {
			val reference = element.reference
			if(reference.resolve() != null) return
			val quickFixes = buildList {
				//要求封装变量引用可读且在合适的位置
				if(element.isWritable) {
					val variableName = element.name
					val parentDefinition = element.findParentDefinition()?.castOrNull<ParadoxScriptProperty>()
					if(parentDefinition != null) {
						this += IntroduceLocalVariableFix(parentDefinition, variableName)
						this += IntroduceGlobalVariableFix(parentDefinition, variableName)
					}
				}
				this += ImportGameOrModDirectoryFix(element)
			}.toTypedArray()
			holder.registerProblem(element, PlsBundle.message("script.inspection.unresolvedScriptedVariable.description", element.name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, *quickFixes)
		}
	}
	
	@Suppress("NAME_SHADOWING")
	private class IntroduceLocalVariableFix(
		element: ParadoxScriptProperty,
		private val variableName: String
	) : LocalQuickFixAndIntentionActionOnPsiElement(element), HighPriorityAction {
		override fun getFamilyName() = PlsBundle.message("script.inspection.unresolvedScriptedVariable.fix.1", variableName)
		
		override fun getText() = PlsBundle.message("script.inspection.unresolvedScriptedVariable.fix.1", variableName)
		
		override fun getPriority() = PriorityAction.Priority.TOP
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			//在所属定义之前另起一行（跳过注释和空白），声明对应名字的封装变量，默认值给0并选中
			val parentDefinition = startElement.cast<ParadoxScriptProperty>()
			introduceScriptedVariable(variableName, "0", parentDefinition, project, editor) { newVariable, editor ->
				val textRange = newVariable.variableValue!!.textRange
				editor.selectionModel.setSelection(textRange.startOffset, textRange.endOffset)
				editor.caretModel.moveToOffset(textRange.endOffset)
				editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
			}
		}
	}
	
	private class IntroduceGlobalVariableFix(
		element: ParadoxScriptProperty,
		private val variableName: String,
	) : LocalQuickFixAndIntentionActionOnPsiElement(element), HighPriorityAction {
		override fun getFamilyName() = PlsBundle.message("script.inspection.unresolvedScriptedVariable.fix.2", variableName)
		
		override fun getText() = PlsBundle.message("script.inspection.unresolvedScriptedVariable.fix.2", variableName)
		
		override fun getPriority() = PriorityAction.Priority.HIGH
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			//在新建或者选择的文件最后另起一行，声明对应名字的封装变量，默认值给0并选中
			val parentDefinition = startElement.cast<ParadoxScriptProperty>()
			//TODO
		}
	}
}

