package icu.windea.pls.core.quickfix

import com.intellij.codeInsight.intention.*
import com.intellij.codeInspection.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.tool.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.refactoring.*

class IntroduceGlobalVariableFix(
	private val variableName: String,
	element: ParadoxScriptedVariableReference,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
	override fun getPriority() = PriorityAction.Priority.HIGH
	
	override fun getText() = PlsBundle.message("script.inspection.advanced.unresolvedScriptedVariable.quickfix.2", variableName)
	
	override fun getFamilyName() = text
	
	override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
		//打开对话框
		val virtualFile = file.virtualFile ?: return
		val scriptedVariablesDirectory = ParadoxFileLocator.getScriptedVariablesDirectory(virtualFile) ?: return //不期望的结果
		val dialog = IntroduceGlobalScriptedVariableDialog(project, scriptedVariablesDirectory, variableName, "0")
		if(!dialog.showAndGet()) return //取消
		
		//在指定脚本文件中声明对应名字的封装变量，默认值给0并选中
		//声明完成后不自动跳转到那个脚本文件
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
	
	override fun startInWriteAction() = false
	
	override fun availableInBatchMode() = false
}
