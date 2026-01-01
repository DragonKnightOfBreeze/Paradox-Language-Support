package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.undo.UndoUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.psi.ParadoxPsiManager
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.refactoring.actions.IntroduceGlobalScriptedVariableDialog
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.script.psi.ParadoxScriptFile

class IntroduceGlobalVariableFix(
    private val variableName: String,
    element: ParadoxScriptedVariableReference,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getText() = PlsBundle.message("fix.introduceGlobalScriptedVariable.name", variableName)

    override fun getFamilyName() = PlsBundle.message("fix.introduceGlobalScriptedVariable.familyName")

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        // 打开对话框
        val virtualFile = file.virtualFile ?: return
        val scriptedVariablesDirectory = ParadoxFileManager.getScriptedVariablesDirectory(virtualFile) ?: return
        val dialog = IntroduceGlobalScriptedVariableDialog(project, scriptedVariablesDirectory, variableName, "0")
        if (!dialog.showAndGet()) return // 取消

        val variableNameToUse = dialog.variableName
        val variableValueToUse = dialog.variableValue
        val targetFile = dialog.file.toPsiFile(project) ?: return // 不期望的结果
        if (targetFile !is ParadoxScriptFile) return

        val command = Runnable {
            // 在指定脚本文件中声明对应名字的封装变量
            ParadoxPsiManager.introduceGlobalScriptedVariable(variableNameToUse, variableValueToUse, targetFile, project)

            val targetDocument = PsiDocumentManager.getInstance(project).getDocument(targetFile)
            if (targetDocument != null) PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(targetDocument) // 提交文档更改

            // 不移动光标（声明后不自动跳转到目标脚本文件）

            // 标记文件更改
            UndoUtil.markPsiFileForUndo(targetFile)
        }
        WriteCommandAction.runWriteCommandAction(project, PlsBundle.message("script.command.introduceGlobalScriptedVariable.name"), null, command, targetFile)
    }

    override fun startInWriteAction() = false

    override fun availableInBatchMode() = false
}
