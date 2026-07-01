package icu.windea.pls.lang.fixes

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.executeWriteCommand
import icu.windea.pls.lang.psi.ParadoxPsiService
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.script.psi.ParadoxScriptFile

class IntroduceLocalScriptedVariableFix(
    private val variableName: String,
    element: ParadoxScriptedVariableReference
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    override fun getPriority() = PriorityAction.Priority.TOP

    override fun getText() = ChronicleBundle.message("fix.introduceLocalScriptedVariable.name", variableName)

    override fun getFamilyName() = ChronicleBundle.message("fix.introduceLocalScriptedVariable.familyName")

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val element = startElement
        val containerElement = selectScope { element.parentDefinitionCandidate() } ?: element.containingFile as? ParadoxScriptFile ?: return

        val commandName = ChronicleBundle.message("script.command.introduceLocalScriptedVariable.name")
        executeWriteCommand(project, commandName, makeWritable = file) c@{
            // 声明对应名字的封装变量，默认值给0
            val newVariable = ParadoxPsiService.introduceLocalScriptedVariable(variableName, "0", containerElement, project)

            val document = PsiDocumentManager.getInstance(project).getDocument(file)
            if (document != null) PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document) // 提交文档更改
            if (editor == null) return@c

            // 光标移到 newVariableValue 的结束位置并选中
            val newVariableValue = newVariable.scriptedVariableValue ?: return@c
            editor.caretModel.moveToOffset(newVariableValue.endOffset)
            editor.selectionModel.setSelection(newVariableValue.startOffset, newVariableValue.endOffset)
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
    }

    override fun startInWriteAction() = false

    override fun availableInBatchMode() = false
}
