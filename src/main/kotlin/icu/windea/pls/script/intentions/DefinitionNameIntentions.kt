package icu.windea.pls.script.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.codeInsight.navigation.*
import com.intellij.codeInsight.navigation.actions.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

abstract class DefinitionNameIntention : IntentionAction, PriorityAction {
	override fun getPriority() = PriorityAction.Priority.LOW
	
	override fun startInWriteAction() = false
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		if(file.language != ParadoxScriptLanguage) return false
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return false
		return element.isDefinitionName()
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		if(file.language != ParadoxScriptLanguage) return
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return
		val definition = element.findParentDefinition() ?: return //unexpected
		val definitionInfo = definition.definitionInfo ?: return //unexpected
		if(element.value != definitionInfo.name) return //unexpected
		doInvoke(definition, definitionInfo, editor, project)
	}
	
	private fun findElement(file: PsiFile, offset: Int): ParadoxScriptString? {
		return file.findElementAtCaret(offset) { it.parent as? ParadoxScriptString }
	}
	
	abstract fun doInvoke(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo, editor: Editor, project: Project)
}

/**
 * 为表示定义名称的字符串提供查找使用的功能的意向。
 */
class DefinitionNameFindUsagesIntention : DefinitionNameIntention() {
	override fun getText() = PlsBundle.message("script.intention.definitionName.findUsages")
	
	override fun doInvoke(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo, editor: Editor, project: Project) {
		GotoDeclarationAction.startFindUsages(editor, project, definition)
	}
	
	override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
		return IntentionPreviewInfo.EMPTY
	}
}

/**
 * 为表示定义名称的字符串提供导航到声明的功能的意向。
 */
class DefinitionNameGotoTypeDeclarationIntention : DefinitionNameIntention() {
	override fun getText() = PlsBundle.message("script.intention.definitionName.gotoTypeDeclaration")
	
	override fun doInvoke(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo, editor: Editor, project: Project) {
		//不包括子类型（`subtype[origin]`）
		val resolved = definitionInfo.typeConfig.pointer.element ?: return
		NavigationUtil.activateFileWithPsiElement(resolved)
	}
	
	override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
		return IntentionPreviewInfo.EMPTY
	}
}
