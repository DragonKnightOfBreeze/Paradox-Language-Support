package icu.windea.pls.script.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.navigation.*
import com.intellij.find.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

abstract class DefinitionNameIntention: IntentionAction, PriorityAction {
	override fun getPriority() = PriorityAction.Priority.LOW
	
	override fun startInWriteAction() = false
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		if(file.language != ParadoxScriptLanguage) return false
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return false
		val element = originalElement.parentOfType<ParadoxScriptString>() ?: return false
		return element.isDefinitionName()
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		if(file.language != ParadoxScriptLanguage) return
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return
		val element = originalElement.parentOfType<ParadoxScriptString>() ?: return
		val definition = element.findParentDefinition() ?: return //unexpected
		val definitionInfo = definition.definitionInfo ?: return //unexpected
		if(element.value != definitionInfo.name) return //unexpected
		doInvoke(definition, definitionInfo, editor, project)
	}
	
	abstract fun doInvoke(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo, editor: Editor, project: Project)
}

/**
 * 为表示定义名称的字符串提供查找使用的功能的意向。注解器提供的提示同样会附带上这个意向。
 */
class DefinitionNameFindUsagesIntention: DefinitionNameIntention() {
	override fun getText() = PlsBundle.message("script.intention.definitionName.findUsages")
	
	override fun doInvoke(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo, editor: Editor, project: Project) {
		ActionUtil.invokeAction(FindUsagesAction(), editor.contentComponent, ActionPlaces.EDITOR_POPUP, null, null)
	}
}

/**
 * 为表示定义名称的字符串提供导航到声明的功能的意向。注解器提供的提示同样会附带上这个意向。
 */
class DefinitionNameGotoTypeDeclarationIntention: DefinitionNameIntention() {
	override fun getText() = PlsBundle.message("script.intention.definitionName.gotoTypeDeclaration")
	
	override fun doInvoke(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo, editor: Editor, project: Project) {
		val element = definitionInfo.typeConfig.pointer.element ?: return
		NavigationUtil.activateFileWithPsiElement(element)
	}
}