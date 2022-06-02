package icu.windea.pls.script.refactoring

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.core.refactoring.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*

/**
 * 声明全局封装变量的重构。
 */
object ParadoxScriptIntroduceGlobalScriptedVariableHandler : ContextAwareRefactoringActionHandler() {
	override fun isAvailable(editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
		if(file.virtualFile == null) return false
		val offset = editor.caretModel.offset
		val position = file.findElementAt(offset) ?: return false
		val positionType = position.elementType
		if(positionType != ParadoxScriptElementTypes.INT_TOKEN && positionType != ParadoxScriptElementTypes.FLOAT_TOKEN) return false
		return position.findParentDefinition()?.castOrNull<ParadoxScriptProperty>() != null
	}
	
	@Suppress("UnstableApiUsage")
	override fun invokeAction(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
		val virtualFile = file.virtualFile ?: return false
		val offset = editor.caretModel.offset
		val position = file.findElementAt(offset) ?: return false
		val positionType = position.elementType
		if(positionType != ParadoxScriptElementTypes.INT_TOKEN && positionType != ParadoxScriptElementTypes.FLOAT_TOKEN) return false
		val name = defaultScriptedVariableName
		
		//将光标移到int_token或float_token的开始并选中
		editor.caretModel.moveToOffset(position.startOffset)
		editor.selectionModel.setSelection(position.startOffset, position.endOffset)
		
		//打开对话框
		val scriptedVariablesDirectory = ParadoxFileLocator.getScriptedVariablesDirectory(virtualFile) ?: return true //不期望的结果
		val dialog = IntroduceGlobalScriptedVariableDialog(project, scriptedVariablesDirectory)
		if(!dialog.showAndGet()) return true //取消
		
		val targetFile = dialog.file.toPsiFile<ParadoxScriptFile>(project) ?: return true //不期望的结果
		val command = Runnable {
			//用封装属性引用（variableReference）替换当前位置的int或float
			val newVariableReference = ParadoxScriptElementFactory.createVariableReference(project, name)
			position.parent.replace(newVariableReference)
			
			//在指定的文件中声明对应的封装变量
			val value = position.text
			ParadoxScriptIntroducer.introduceGlobalScriptedVariable(name, value, targetFile, project)
			val document = PsiDocumentManager.getInstance(project).getDocument(targetFile)
			if(document != null) PsiDocumentManager.getInstance(project).commitDocument(document) //提交文档更改
		}
		WriteCommandAction.runWriteCommandAction(project, PlsBundle.message("script.command.introduceGlobalScriptedVariable.text"), null, command, targetFile)
		
		//回到原来的光标位置
		editor.caretModel.moveToOffset(position.startOffset)
		editor.selectionModel.removeSelection()
		editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
		return true
	}
}