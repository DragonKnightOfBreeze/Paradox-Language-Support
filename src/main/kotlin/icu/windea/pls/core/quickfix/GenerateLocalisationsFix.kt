package icu.windea.pls.core.quickfix

import com.intellij.codeInsight.intention.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.codeInsight.generation.*
import icu.windea.pls.script.psi.*

class GenerateLocalisationsFix(
	private val localisationNames: Set<String>,
	private val definitionName: String,
	element: ParadoxScriptDefinitionElement
): LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
	override fun getPriority() = PriorityAction.Priority.HIGH
	
	override fun getText() = PlsBundle.message("inspection.script.general.missingLocalisation.quickfix.1", definitionName)
	
	override fun getFamilyName() = text
	
	override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
		if(editor == null) return
		if(startElement !is ParadoxScriptDefinitionElement) return
		val handler = GenerateLocalisationsHandler(localisationNames, startElement)
		return handler.invoke(project, editor, file)
	}
	
	//true so that we can run MissingLocalisationInspection on mod files scope and generate all missing localisations
	override fun availableInBatchMode() = true
}