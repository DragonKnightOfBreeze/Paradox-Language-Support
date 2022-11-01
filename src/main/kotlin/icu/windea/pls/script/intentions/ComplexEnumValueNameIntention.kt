package icu.windea.pls.script.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.codeInsight.navigation.*
import com.intellij.codeInsight.navigation.actions.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.*

abstract class ComplexEnumValueNameIntention: IntentionAction, PriorityAction {
	override fun getPriority() = PriorityAction.Priority.LOW
	
	override fun startInWriteAction() = false
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		if(file.language != ParadoxScriptLanguage) return false
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return false
		val element = originalElement.parentOfType<ParadoxExpressionAwareElement>() ?: return false
		return element.complexEnumValueInfo != null
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		if(file.language != ParadoxScriptLanguage) return
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return
		val element = originalElement.parentOfType<ParadoxExpressionAwareElement>() ?: return
		val complexEnumValueInfo = element.complexEnumValueInfo ?: return
		doInvoke(element, complexEnumValueInfo, editor, project)
	}
	
	abstract fun doInvoke(element: ParadoxExpressionAwareElement, complexEnumValueInfo: ParadoxComplexEnumValueInfo, editor: Editor, project: Project)
}

/**
 * 为表示复杂枚举名称的表达式提供查找使用的功能的意向。
 */
class ComplexEnumValueNameFindUsagesIntention: ComplexEnumValueNameIntention() {
	override fun getText() = PlsBundle.message("script.intention.complexEnumValueName.findUsages")
	
	override fun doInvoke(element: ParadoxExpressionAwareElement, complexEnumValueInfo: ParadoxComplexEnumValueInfo, editor: Editor, project: Project) {
		//TODO
		GotoDeclarationAction.startFindUsages(editor, project, element)
	}
	
	override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
		return IntentionPreviewInfo.EMPTY
	}
}

/**
 * 为表示复杂枚举名称的表达式提供导航到声明的功能的意向。
 */
class ComplexEnumValueNameGotoTypeDeclarationIntention: ComplexEnumValueNameIntention() {
	override fun getText() = PlsBundle.message("script.intention.complexEnumValueName.gotoTypeDeclaration")
	
	override fun doInvoke(definition: ParadoxExpressionAwareElement, complexEnumValueInfo: ParadoxComplexEnumValueInfo, editor: Editor, project: Project) {
		val gameType = complexEnumValueInfo.gameType ?: return
		val configGroup = getCwtConfig(project).getValue(gameType)
		val enumName = complexEnumValueInfo.enumName
		val config = configGroup.complexEnums[enumName] ?: return //unexpected
		val element = config.pointer.element ?: return
		NavigationUtil.activateFileWithPsiElement(element)
	}
	
	override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
		return IntentionPreviewInfo.EMPTY
	}
}