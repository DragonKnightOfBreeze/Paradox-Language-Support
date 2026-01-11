package icu.windea.pls.lang.refactoring.actions

import com.intellij.codeInsight.template.TemplateBuilderFactory
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.impl.FinishMarkAction
import com.intellij.openapi.command.impl.StartMarkAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.buildInlineTemplate
import icu.windea.pls.core.cast
import icu.windea.pls.core.codeInsight.TemplateEditingFinishedListener
import icu.windea.pls.core.executeWriteCommand
import icu.windea.pls.core.findElementAt
import icu.windea.pls.lang.psi.ParadoxPsiManager
import icu.windea.pls.lang.psi.parentPropertyDefinitionOrInjection
import icu.windea.pls.lang.psi.search
import icu.windea.pls.lang.refactoring.ContextAwareRefactoringActionHandler
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

/**
 * 声明本地封装变量的重构。
 */
class IntroduceLocalScriptedVariableHandler : ContextAwareRefactoringActionHandler() {
    override fun isAvailable(editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return false
        return element.search { parentPropertyDefinitionOrInjection() } != null
    }

    @Suppress("UnstableApiUsage")
    override fun invokeAction(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return false
        val name = PlsInternalSettings.getInstance().defaultScriptedVariableName

        // 将光标移到 element 的结束位置并选中
        editor.caretModel.moveToOffset(element.endOffset)
        editor.selectionModel.setSelection(element.startOffset, element.endOffset)

        // 要求对应的字面量在定义声明内
        // 2.1.0 兼容定义注入
        val containerElement = element.search { parentPropertyDefinitionOrInjection() } ?: return false

        val commandName = PlsBundle.message("script.command.introduceLocalScriptedVariable.name")
        executeWriteCommand(project, commandName, makeWritable = file) {
            // 用封装变量引用替换当前位置的字面量
            var newVariableReference = ParadoxScriptElementFactory.createVariableReference(project, name)
            newVariableReference = element.parent.replace(newVariableReference).cast<ParadoxScriptScriptedVariableReference>()

            // 声明对应名字的封装变量，以内联模板的方式编辑名字
            val variableValue = element.text
            val newVariable = ParadoxPsiManager.introduceLocalScriptedVariable(name, variableValue, containerElement, project)
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document) // 提交文档更改

            val startAction = StartMarkAction.start(editor, project, commandName)
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
                    // 回到原来的光标位置
                    editor.caretModel.moveToOffset(caretMarker.endOffset)
                    editor.selectionModel.removeSelection()
                    editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
                } finally {
                    FinishMarkAction.finish(project, editor, startAction)
                }
            })
        }

        return true
    }

    private fun findElement(file: PsiFile, offset: Int): PsiElement? {
        return file.findElementAt(offset) { it.takeIf { ParadoxScriptTokenSets.SCRIPTED_VARIABLE_VALUE_TOKENS.contains(it.elementType) } }
    }
}
