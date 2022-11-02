package icu.windea.pls.script.refactoring

import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.*
import com.intellij.openapi.command.impl.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.refactoring.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

/**
 * 声明本地封装变量的重构。
 */
object ParadoxScriptIntroduceLocalScriptedVariableHandler : ContextAwareRefactoringActionHandler() {
	override fun isAvailable(editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
		val offset = editor.caretModel.offset
		val position = file.findElementAt(offset) ?: return false
		val positionType = position.elementType
		if(positionType != INT_TOKEN && positionType != FLOAT_TOKEN) return false
		return position.findParentDefinition()?.castOrNull<ParadoxScriptProperty>() != null
	}
	
	@Suppress("UnstableApiUsage")
	override fun invokeAction(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
		val offset = editor.caretModel.offset
		val position = file.findElementAt(offset) ?: return false
		val positionType = position.elementType
		if(positionType != INT_TOKEN && positionType != FLOAT_TOKEN) return false
		val name = PlsConstants.defaultScriptedVariableName
		
		//将光标移到所在PSI元素的结束位置并选中
		editor.caretModel.moveToOffset(position.endOffset)
		editor.selectionModel.setSelection(position.startOffset, position.endOffset)
		
		//要求对应的int_token或float_token在定义声明内
		val parentDefinition = position.findParentDefinition()?.castOrNull<ParadoxScriptProperty>() ?: return false
		val command = Runnable {
			//用封装属性引用（variableReference）替换当前位置的int或float
			var newVariableReference = ParadoxScriptElementFactory.createVariableReference(project, name)
			newVariableReference = position.parent.replace(newVariableReference).cast()
			val variableReferenceId = newVariableReference.variableReferenceId
			
			//声明对应名字的封装变量，以内联模版的方式编辑变量名
			val variableValue = position.text
			val newVariable = ParadoxScriptIntroducer.introduceLocalScriptedVariable(name, variableValue, parentDefinition, project)
			PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document) //提交文档更改
			
			val startAction = StartMarkAction.start(editor, project, PlsBundle.message("script.command.introduceLocalScriptedVariable.name"))
			val builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(file)
			val variableNameId = newVariable.variableName.variableNameId
			builder.replaceElement(variableNameId, "variableName", TextExpression(variableNameId.text), true)
			builder.replaceElement(variableReferenceId, "variableReference", "variableName", false)
			val caretMarker = editor.document.createRangeMarker(0, editor.caretModel.offset)
			caretMarker.isGreedyToRight = true
			editor.caretModel.moveToOffset(0)
			val template = builder.buildInlineTemplate()
			TemplateManager.getInstance(project).startTemplate(editor, template, TemplateEditingFinishedListener { _, _ ->
				try {
					//回到原来的光标位置
					editor.caretModel.moveToOffset(caretMarker.endOffset)
					editor.selectionModel.removeSelection()
					editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
				} finally {
					FinishMarkAction.finish(project, editor, startAction)
				}
			})
		}
		WriteCommandAction.runWriteCommandAction(project, PlsBundle.message("script.command.introduceLocalScriptedVariable.name"), null, command, file)
		return true
	}
}