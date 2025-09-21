package icu.windea.pls.lang.refactoring.actions

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
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
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findElementAt
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.refactoring.ContextAwareRefactoringActionHandler
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.lang.util.psi.ParadoxPsiManager
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.findParentDefinition

/**
 * 声明全局封装变量的重构。
 */
class IntroduceGlobalScriptedVariableHandler : ContextAwareRefactoringActionHandler() {
    override fun isAvailable(editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
        if (file.virtualFile == null) return false
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
        val scriptedVariablesDirectory = ParadoxFileManager.getScriptedVariablesDirectory(virtualFile) ?: return true //不期望的结果
        val dialog = IntroduceGlobalScriptedVariableDialog(project, scriptedVariablesDirectory, PlsFacade.getInternalSettings().defaultScriptedVariableName)
        if (!dialog.showAndGet()) return true //取消

        val variableName = dialog.variableName
        val variableValue = element.text
        val targetFile = dialog.file.toPsiFile(project) ?: return true //不期望的结果
        if (targetFile !is ParadoxScriptFile) return true
        val command = Runnable {
            //用封装参数（variableReference）替换当前位置的int或float
            val createdVariableReference = ParadoxScriptElementFactory.createVariableReference(project, variableName)
            val newVariableReference = element.parent.replace(createdVariableReference)

            val document = PsiDocumentManager.getInstance(project).getDocument(file)
            if (document != null) PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document) //提交文档更改

            //在指定的文件中声明对应的封装变量
            ParadoxPsiManager.introduceGlobalScriptedVariable(variableName, variableValue, targetFile, project)
            val targetDocument = PsiDocumentManager.getInstance(project).getDocument(targetFile)
            if (targetDocument != null) PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(targetDocument) //提交文档更改

            //光标移到newVariableReference的结束位置
            editor.caretModel.moveToOffset(newVariableReference.endOffset)
            editor.selectionModel.removeSelection()
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
        WriteCommandAction.runWriteCommandAction(project, PlsBundle.message("script.command.introduceGlobalScriptedVariable.name"), null, command, file, targetFile)

        return true
    }

    private fun findElement(file: PsiFile, offset: Int): PsiElement? {
        return file.findElementAt(offset) { it.takeIf { ParadoxScriptTokenSets.SCRIPTED_VARIABLE_VALUE_TOKENS.contains(it.elementType) } }
    }
}
