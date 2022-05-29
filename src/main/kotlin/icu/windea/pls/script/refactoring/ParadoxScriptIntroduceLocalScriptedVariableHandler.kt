package icu.windea.pls.script.refactoring

import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.command.impl.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

/**
 * 声明本地封装变量的重构。
 */
object ParadoxScriptIntroduceLocalScriptedVariableHandler : RefactoringActionHandler {
	private const val commandName = "Introduce Local Scripted Variable"
	
	@Suppress("UnstableApiUsage")
	override fun invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext?) {
		val offset = editor.caretModel.offset
		val position = file.findElementAt(offset) ?: return
		val positionType = position.elementType
		if(positionType != INT_TOKEN && positionType != FLOAT_TOKEN) return
		val parentDefinition = position.findParentDefinition()?.castOrNull<ParadoxScriptProperty>() ?: return
		//在所属定义之前另起一行（跳过注释和空白），声明对应名字的封装变量，默认值给0，要求用户编辑变量名
		runUndoTransparentWriteAction {
			val value = position.text
			val name = defaultScriptedVariableName
			introduceScriptedVariable(name, value, parentDefinition, project, editor) { newVariable, editor ->
				//用封装属性引用（variableReference）替换当前位置的int或float
				var newVariableReference = ParadoxScriptElementFactory.createVariableReference(project, name)
				newVariableReference = position.parent.replace(newVariableReference).cast()
				val variableReferenceId = newVariableReference.variableReferenceId
				
				val startAction = StartMarkAction.start(editor, project, commandName)
				val builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(newVariable.parent)
				val variableNameId = newVariable.variableName.variableNameId
				builder.replaceElement(variableNameId, "variableName", TextExpression(variableNameId.text), true)
				builder.replaceElement(variableReferenceId, "variableReference", "variableName", false)
				val caretMarker = editor.document.createRangeMarker(0, editor.caretModel.offset)
				caretMarker.isGreedyToRight = true
				editor.caretModel.moveToOffset(0)
				val template = builder.buildInlineTemplate()
				template.isToReformat = true
				TemplateManager.getInstance(project).startTemplate(editor, template, TemplateEditingFinishedListener { _, _ ->
					try {
						//回到原来的光标位置
						editor.caretModel.moveToOffset(caretMarker.endOffset)
						editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
					} finally {
						FinishMarkAction.finish(project, editor, startAction)
					}
				})
			}
		}
	}
	
	override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
		//not support
	}
}