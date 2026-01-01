package icu.windea.pls.lang.refactoring.actions

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.CommandProcessor
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
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.executeWriteCommand
import icu.windea.pls.core.findElementAt
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.psi.ParadoxPsiManager
import icu.windea.pls.lang.psi.findParentDefinitionOrInjection
import icu.windea.pls.lang.refactoring.ContextAwareRefactoringActionHandler
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

/**
 * 声明全局封装变量的重构。
 */
class IntroduceGlobalScriptedVariableHandler : ContextAwareRefactoringActionHandler() {
    override fun isAvailable(editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
        if (file.virtualFile == null) return false
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return false
        return element.findParentDefinitionOrInjection()?.castOrNull<ParadoxScriptProperty>() != null
    }

    override fun invokeAction(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
        val virtualFile = file.virtualFile ?: return false
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return false

        // 将光标移到字面量的开始并选中
        editor.caretModel.moveToOffset(element.startOffset)
        editor.selectionModel.setSelection(element.startOffset, element.endOffset)

        // 打开对话框
        val scriptedVariablesDirectory = ParadoxFileManager.getScriptedVariablesDirectory(virtualFile) ?: return true
        val dialog = IntroduceGlobalScriptedVariableDialog(project, scriptedVariablesDirectory, PlsInternalSettings.getInstance().defaultScriptedVariableName)
        if (!dialog.showAndGet()) return true // 取消

        val variableNameToUse = dialog.variableName
        val variableValueToUse = element.text
        val targetFile = dialog.file.toPsiFile(project) ?: return true // 不期望的结果
        if (targetFile !is ParadoxScriptFile) return true

        val commandName = PlsBundle.message("script.command.introduceGlobalScriptedVariable.name")
        executeWriteCommand(project, commandName, makeWritable = setOf(file, targetFile)) {
            // 标记为全局命令
            CommandProcessor.getInstance().markCurrentCommandAsGlobal(project)

            // 用封装变量引用替换当前位置的字面量
            val createdVariableReference = ParadoxScriptElementFactory.createVariableReference(project, variableNameToUse)
            val newVariableReference = element.parent.replace(createdVariableReference)

            val document = PsiDocumentManager.getInstance(project).getDocument(file)
            if (document != null) PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document) // 提交文档更改

            // 在指定脚本文件中声明对应名字的封装变量
            ParadoxPsiManager.introduceGlobalScriptedVariable(variableNameToUse, variableValueToUse, targetFile, project)
            val targetDocument = PsiDocumentManager.getInstance(project).getDocument(targetFile)
            if (targetDocument != null) PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(targetDocument) // 提交文档更改

            // 光标移到新的封装变量引用的结束位置（声明后自动跳转到目标脚本文件）
            editor.caretModel.moveToOffset(newVariableReference.endOffset)
            editor.selectionModel.removeSelection()
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }

        return true
    }

    private fun findElement(file: PsiFile, offset: Int): PsiElement? {
        return file.findElementAt(offset) { it.takeIf { ParadoxScriptTokenSets.SCRIPTED_VARIABLE_VALUE_TOKENS.contains(it.elementType) } }
    }
}
