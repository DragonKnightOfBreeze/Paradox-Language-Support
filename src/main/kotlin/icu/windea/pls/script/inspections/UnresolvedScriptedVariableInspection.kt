package icu.windea.pls.script.inspections

import com.intellij.codeInsight.intention.*
import com.intellij.codeInspection.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.core.tool.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.refactoring.*

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
			ProgressManager.checkCanceled()
			val reference = element.reference
			if(reference.resolve() != null) return
			val quickFixes = buildList {
				//要求对应的variableReference在定义声明内
				val variableName = element.name
				val parentDefinition = element.findParentDefinition()?.castOrNull<ParadoxScriptProperty>()
				if(parentDefinition != null) {
					this += IntroduceLocalVariableFix(variableName, parentDefinition)
					this += IntroduceGlobalVariableFix(variableName, parentDefinition)
					this += ImportGameOrModDirectoryFix(element)
				}
			}.toTypedArray<LocalQuickFix>()
			holder.registerProblem(element, PlsBundle.message("script.inspection.unresolvedScriptedVariable.description", element.name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, *quickFixes)
		}
	}
	
	private class IntroduceLocalVariableFix(
		private val variableName: String,
		element: ParadoxScriptProperty
	) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
		override fun getPriority() = PriorityAction.Priority.TOP
		
		override fun getText() = PlsBundle.message("script.inspection.unresolvedScriptedVariable.quickfix.1", variableName)
		
		override fun getFamilyName() = text
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			val command = Runnable {
				//声明对应名字的封装变量，默认值给0
				val parentDefinition = startElement.cast<ParadoxScriptProperty>()
				val newVariable = ParadoxScriptIntroducer.introduceLocalScriptedVariable(variableName, "0", parentDefinition, project)
				
				val document = PsiDocumentManager.getInstance(project).getDocument(file)
				if(document != null) PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document) //提交文档更改
				if(editor != null) {
					//光标移到newVariableValue的结束位置并选中
					val newVariableValue = newVariable.scriptedVariableValue ?: return@Runnable
					editor.caretModel.moveToOffset(newVariableValue.endOffset)
					editor.selectionModel.setSelection(newVariableValue.startOffset, newVariableValue.endOffset)
					editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
				}
			}
			WriteCommandAction.runWriteCommandAction(project, PlsBundle.message("script.command.introduceLocalScriptedVariable.name"), null, command, file)
		}
		
		override fun availableInBatchMode() = false
		
		override fun startInWriteAction() = false
	}
	
	private class IntroduceGlobalVariableFix(
		private val variableName: String,
		element: ParadoxScriptProperty,
	) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
		override fun getPriority() = PriorityAction.Priority.HIGH
		
		override fun getText() = PlsBundle.message("script.inspection.unresolvedScriptedVariable.quickfix.2", variableName)
		
		override fun getFamilyName() = text
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			//打开对话框
			val virtualFile = file.virtualFile ?: return
			val scriptedVariablesDirectory = ParadoxFileLocator.getScriptedVariablesDirectory(virtualFile) ?: return //不期望的结果
			val dialog = IntroduceGlobalScriptedVariableDialog(project, scriptedVariablesDirectory, variableName, "0")
			if(!dialog.showAndGet()) return //取消
			
			//声明对应名字的封装变量，默认值给0并选中
			val variableNameToUse = dialog.variableName
			val variableValue = dialog.variableValue
			val targetFile = dialog.file.toPsiFile<ParadoxScriptFile>(project) ?: return //不期望的结果
			val command = Runnable {
				ParadoxScriptIntroducer.introduceGlobalScriptedVariable(variableNameToUse, variableValue, targetFile, project)
				
				val targetDocument = PsiDocumentManager.getInstance(project).getDocument(targetFile)
				if(targetDocument != null) PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(targetDocument) //提交文档更改
				
				//不移动光标
			}
			WriteCommandAction.runWriteCommandAction(project, PlsBundle.message("script.command.introduceGlobalScriptedVariable.name"), null, command, targetFile)
		}
		
		override fun availableInBatchMode() = false
		
		override fun startInWriteAction() = false
	}
}

