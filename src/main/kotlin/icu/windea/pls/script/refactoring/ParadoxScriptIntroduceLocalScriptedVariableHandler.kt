package icu.windea.pls.script.refactoring

import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.codeInsight.*
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
		val parentDefinition = position.findParentDefinition()?.castOrNull<ParadoxScriptProperty>() ?: return false
		
		//在所属定义之前另起一行（跳过注释和空白），声明对应名字的封装变量，默认值给0，要求用户编辑变量名
		val command = Runnable {
			val value = position.text
			val name = defaultScriptedVariableName
			introduceScriptedVariable(name, value, parentDefinition, project, editor) { newVariable, editor ->
				//用封装属性引用（variableReference）替换当前位置的int或float
				var newVariableReference = ParadoxScriptElementFactory.createVariableReference(project, name)
				newVariableReference = position.parent.replace(newVariableReference).cast()
				val variableReferenceId = newVariableReference.variableReferenceId
				
				//参照Kotlin的相关实现，完成后光标不需要回到原来的位置
				//val startAction = StartMarkAction.start(editor, project, commandName)
				val builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(newVariable.parent)
				val variableNameId = newVariable.variableName.variableNameId
				builder.replaceElement(variableNameId, "variableName", TextExpression(variableNameId.text), true)
				builder.replaceElement(variableReferenceId, "variableReference", "variableName", false)
				//val caretMarker = editor.document.createRangeMarker(0, editor.caretModel.offset)
				//caretMarker.isGreedyToRight = true
				//editor.caretModel.moveToOffset(0)
				val template = builder.buildInlineTemplate()
				template.isToReformat = true
				TemplateManager.getInstance(project).startTemplate(editor, template)
				//TemplateManager.getInstance(project).startTemplate(editor, template, TemplateEditingFinishedListener { _, _ ->
				//	try {
				//		//回到原来的光标位置
				//		editor.caretModel.moveToOffset(caretMarker.endOffset)
				//		editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
				//	} finally {
				//		FinishMarkAction.finish(project, editor, startAction)
				//	}
				//})
			}
		}
		WriteCommandAction.runWriteCommandAction(project, PlsBundle.message("script.command.introduceLocalScriptedVariable.text"), null, command, file)
		return true
	}
}