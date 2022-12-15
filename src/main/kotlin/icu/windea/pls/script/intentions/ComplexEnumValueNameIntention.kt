package icu.windea.pls.script.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.codeInsight.navigation.*
import com.intellij.codeInsight.navigation.actions.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

abstract class ComplexEnumValueNameIntention : IntentionAction, PriorityAction {
	override fun getPriority() = PriorityAction.Priority.LOW
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return false
		return element.complexEnumValueInfo != null
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return
		val complexEnumValueInfo = element.complexEnumValueInfo ?: return
		doInvoke(element, complexEnumValueInfo, editor, project)
	}
	
	private fun findElement(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
		return file.findElementAt(offset) { it.parent as? ParadoxScriptStringExpressionElement }
	}
	
	abstract fun doInvoke(element: ParadoxScriptStringExpressionElement, complexEnumValueInfo: ParadoxComplexEnumValueInfo, editor: Editor, project: Project)
	
	override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
	
	override fun startInWriteAction() = false
}

/**
 * 为表示复杂枚举名称的表达式提供查找使用的功能的意向。
 * @see icu.windea.pls.core.search.usages.ParadoxComplexEnumValueUsagesSearcher
 */
class ComplexEnumValueNameFindUsagesIntention : ComplexEnumValueNameIntention() {
	override fun getText() = PlsBundle.message("script.intention.complexEnumValueName.findUsages")
	
	override fun doInvoke(element: ParadoxScriptStringExpressionElement, complexEnumValueInfo: ParadoxComplexEnumValueInfo, editor: Editor, project: Project) {
		GotoDeclarationAction.startFindUsages(editor, project, element)
	}
}

/**
 * 为表示复杂枚举名称的表达式提供导航到实现的功能的意向。
 * @see icu.windea.pls.core.search.implementations.ParadoxComplexEnumValueImplementationsSearch
 */
class ComplexEnumValueNameGotoImplementationsIntention : ComplexEnumValueNameIntention() {
	override fun getText() = PlsBundle.message("script.intention.complexEnumValueName.gotoImplementations")
	
	override fun doInvoke(element: ParadoxScriptStringExpressionElement, complexEnumValueInfo: ParadoxComplexEnumValueInfo, editor: Editor, project: Project) {
		val gameType = complexEnumValueInfo.gameType ?: return
		val enumName = complexEnumValueInfo.enumName
		val scope = GlobalSearchScope.allScope(project)
		val selector = complexEnumValueSelector().gameType(gameType)
		val result = ParadoxComplexEnumValueSearch.search(complexEnumValueInfo.name, complexEnumValueInfo.enumName, project, scope, selector).findAll()
		NavigationUtil.getPsiElementPopup(result.toTypedArray(), PlsBundle.message("script.intention.complexEnumValueName.gotoImplementations.title", enumName))
			.showInBestPositionFor(editor)
	}
}

/**
 * 为表示复杂枚举名称的表达式提供导航到声明的功能的意向。
 * @see icu.windea.pls.core.codeInsight.ParadoxTypeDeclarationProvider
 */
class ComplexEnumValueNameGotoTypeDeclarationIntention : ComplexEnumValueNameIntention() {
	override fun getText() = PlsBundle.message("script.intention.complexEnumValueName.gotoTypeDeclaration")
	
	override fun doInvoke(element: ParadoxScriptStringExpressionElement, complexEnumValueInfo: ParadoxComplexEnumValueInfo, editor: Editor, project: Project) {
		val gameType = complexEnumValueInfo.gameType ?: return
		val configGroup = getCwtConfig(project).getValue(gameType)
		val enumName = complexEnumValueInfo.enumName
		val config = configGroup.complexEnums[enumName] ?: return //unexpected
		val resolved = config.pointer.element ?: return
		val render = NameOnlyPsiElementCellRender()
		NavigationUtil.getPsiElementPopup(arrayOf(resolved), render, PlsBundle.message("script.intention.complexEnumValueName.gotoTypeDeclaration.title", enumName))
			.showInBestPositionFor(editor)
	}
	
	override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
		return IntentionPreviewInfo.EMPTY
	}
}
