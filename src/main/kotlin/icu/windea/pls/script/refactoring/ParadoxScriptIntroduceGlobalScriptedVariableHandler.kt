package icu.windea.pls.script.refactoring

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.refactoring.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*

/**
 * 声明全局封装变量的重构。
 */
class ParadoxScriptIntroduceGlobalScriptedVariableHandler : ContextAwareRefactoringActionHandler() {
	override fun isAvailable(editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
		if(file.virtualFile == null) return false
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return false
		return element.findParentDefinition()?.castOrNull<ParadoxScriptProperty>() != null
	}
	
	override fun invokeAction(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
		val virtualFile = file.virtualFile ?: return false
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return false
		
		//将光标移到int_token或float_token的开始并选中
		editor.caretModel.moveToOffset(element.startOffset)
		editor.selectionModel.setSelection(element.startOffset, element.endOffset)
		
		//打开对话框
		val scriptedVariablesDirectory = ParadoxFileLocator.getScriptedVariablesDirectory(virtualFile) ?: return true //不期望的结果
		val dialog = IntroduceGlobalScriptedVariableDialog(project, scriptedVariablesDirectory, PlsConstants.defaultScriptedVariableName)
		if(!dialog.showAndGet()) return true //取消
		
		val variableName = dialog.variableName
		val variableValue = element.text
		val targetFile = dialog.file.toPsiFile<ParadoxScriptFile>(project) ?: return true //不期望的结果
		val command = Runnable {
			//用封装属性引用（variableReference）替换当前位置的int或float
			val createdVariableReference = ParadoxScriptElementFactory.createVariableReference(project, variableName)
			val newVariableReference = element.parent.replace(createdVariableReference)
			
			val document = PsiDocumentManager.getInstance(project).getDocument(file)
			if(document != null) PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document) //提交文档更改
			
			//在指定的文件中声明对应的封装变量
			ParadoxScriptIntroducer.introduceGlobalScriptedVariable(variableName, variableValue, targetFile, project)
			val targetDocument = PsiDocumentManager.getInstance(project).getDocument(targetFile)
			if(targetDocument != null) PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(targetDocument) //提交文档更改
			
			//光标移到newVariableReference的结束位置
			editor.caretModel.moveToOffset(newVariableReference.endOffset)
			editor.selectionModel.removeSelection()
			editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
		}
		WriteCommandAction.runWriteCommandAction(project, PlsBundle.message("script.command.introduceGlobalScriptedVariable.name"), null, command, file, targetFile)
		
		return true
	}
	
	private fun findElement(file: PsiFile, offset: Int): PsiElement? {
		return file.findElementAt(offset) { it.takeIf { ParadoxScriptTokenSets.SCRIPTED_VARIABLE_VALUES.contains(it.elementType) } }
	}
}
