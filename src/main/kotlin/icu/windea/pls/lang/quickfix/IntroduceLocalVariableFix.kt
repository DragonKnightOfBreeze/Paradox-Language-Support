package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.intention.*
import com.intellij.codeInspection.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class IntroduceLocalVariableFix(
	private val variableName: String,
	element: ParadoxScriptedVariableReference
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
	override fun getPriority() = PriorityAction.Priority.TOP
	
	override fun getText() = PlsBundle.message("inspection.script.general.unresolvedScriptedVariable.quickfix.1", variableName)
	
	override fun getFamilyName() = text
	
	override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
		val command = Runnable {
            //声明对应名字的封装变量，默认值给0
            val element = startElement
            val parentDefinitionOrFile = element.findParentDefinition() ?: element.containingFile as? ParadoxScriptFile ?: return@Runnable
            val newVariable = ParadoxPsiManager.introduceLocalScriptedVariable(variableName, "0", parentDefinitionOrFile, project)
            
            val document = PsiDocumentManager.getInstance(project).getDocument(file)
            if(document != null) PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document) //提交文档更改
            if(editor != null) {
                //光标移到newVariableValue的结束位置并选中
                val newVariableValue = newVariable.scriptedVariableValue ?: return@Runnable
                editor.caretModel.moveToOffset(newVariableValue.endOffset)
                editor.selectionModel.setSelection(newVariableValue.startOffset, newVariableValue.endOffset)
                editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
            }
        }
		WriteCommandAction.runWriteCommandAction(project, PlsBundle.message("script.command.introduceLocalScriptedVariable.name"), null, command, file)
	}
	
	override fun startInWriteAction() = false
	
	override fun availableInBatchMode() = false
}
