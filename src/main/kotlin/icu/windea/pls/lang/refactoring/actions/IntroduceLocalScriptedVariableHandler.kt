package icu.windea.pls.lang.refactoring.actions

import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.*
import com.intellij.openapi.command.impl.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.*
import icu.windea.pls.lang.refactoring.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 声明本地封装变量的重构。
 */
class IntroduceLocalScriptedVariableHandler : ContextAwareRefactoringActionHandler() {
	override fun isAvailable(editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return false
		return element.findParentDefinition()?.castOrNull<ParadoxScriptProperty>() != null
	}
	
	@Suppress("UnstableApiUsage")
	override fun invokeAction(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return false
		val name = PlsConstants.Settings.defaultScriptedVariableName
		
		//将光标移到所在PSI元素的结束位置并选中
		editor.caretModel.moveToOffset(element.endOffset)
		editor.selectionModel.setSelection(element.startOffset, element.endOffset)
		
		//要求对应的int_token或float_token在定义声明内
		val parentDefinition = element.findParentDefinition()?.castOrNull<ParadoxScriptProperty>() ?: return false
		val command = Runnable {
            //用封装属性引用（variableReference）替换当前位置的int或float
            var newVariableReference = ParadoxScriptElementFactory.createVariableReference(project, name)
            newVariableReference = element.parent.replace(newVariableReference).cast()
            
            //声明对应名字的封装变量，以内联模版的方式编辑变量名
            val variableValue = element.text
            val newVariable = ParadoxPsiManager.introduceLocalScriptedVariable(name, variableValue, parentDefinition, project)
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document) //提交文档更改
            
            val startAction = StartMarkAction.start(editor, project, PlsBundle.message("script.command.introduceLocalScriptedVariable.name"))
            val templateBuilder = TemplateBuilderFactory.getInstance().createTemplateBuilder(file)
            val variableName = newVariable.scriptedVariableName
            templateBuilder.replaceElement(variableName, "variableName", TextExpression(variableName.text), true)
            templateBuilder.replaceElement(newVariableReference, "variableReference", "variableName", false)
            val caretMarker = editor.document.createRangeMarker(0, editor.caretModel.offset)
            caretMarker.isGreedyToRight = true
            editor.caretModel.moveToOffset(0)
            val template = templateBuilder.buildInlineTemplate()
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
	
	private fun findElement(file: PsiFile, offset: Int): PsiElement? {
		return file.findElementAt(offset) { it.takeIf { ParadoxScriptTokenSets.SCRIPTED_VARIABLE_VALUE_TOKENS.contains(it.elementType) } }
	}
}
