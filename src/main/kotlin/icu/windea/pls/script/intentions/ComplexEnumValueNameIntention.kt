package icu.windea.pls.script.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.codeInsight.navigation.*
import com.intellij.codeInsight.navigation.actions.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

abstract class ComplexEnumValueNameIntention : IntentionAction, PriorityAction, Iconable {
	override fun getIcon(flags: Int) = null
	
	override fun getPriority() = PriorityAction.Priority.HIGH
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return false
		val info = element.complexEnumValueInfo ?: return false
		val gameType = selectGameType(file) ?: return false
		val config = getCwtConfig(project).getValue(gameType).complexEnums.get(info.enumName) 
		return config != null
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return
		val info = element.complexEnumValueInfo ?: return
		val gameType = selectGameType(file) ?: return
		val config = getCwtConfig(project).getValue(gameType).complexEnums.get(info.enumName)
		if(config == null) return
		doInvoke(element, info, config, editor, project)
	}
	
	private fun findElement(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
		return file.findElementAt(offset) { it.parent as? ParadoxScriptStringExpressionElement }
	}
	
	abstract fun doInvoke(element: ParadoxScriptStringExpressionElement, info: ParadoxComplexEnumValueInfo, config: CwtComplexEnumConfig, editor: Editor, project: Project)
	
	override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
	
	override fun startInWriteAction() = false
}

/**
 * 为表示复杂枚举名称的表达式提供查找使用的功能的意向。
 * @see icu.windea.pls.core.search.usages.ParadoxComplexEnumValueUsagesSearcher
 */
class ComplexEnumValueNameFindUsagesIntention : ComplexEnumValueNameIntention() {
	override fun getText() = PlsBundle.message("script.intention.complexEnumValueName.findUsages")
	
	override fun doInvoke(element: ParadoxScriptStringExpressionElement, info: ParadoxComplexEnumValueInfo, config: CwtComplexEnumConfig, editor: Editor, project: Project) {
		GotoDeclarationAction.startFindUsages(editor, project, element)
	}
}

/**
 * 为表示复杂枚举名称的表达式提供导航到实现的功能的意向。
 * @see icu.windea.pls.core.search.implementations.ParadoxComplexEnumValueImplementationsSearch
 */
class ComplexEnumValueNameGotoImplementationsIntention : ComplexEnumValueNameIntention() {
	override fun getText() = PlsBundle.message("script.intention.complexEnumValueName.gotoImplementations")
	
	override fun doInvoke(element: ParadoxScriptStringExpressionElement, info: ParadoxComplexEnumValueInfo, config: CwtComplexEnumConfig, editor: Editor, project: Project) {
		val gameType = info.gameType ?: return
		val enumName = info.enumName
		val searchScope = config.searchScopeType
		val selector = complexEnumValueSelector(project).gameType(gameType).withSearchScopeType(searchScope, element)
		val result = ParadoxComplexEnumValueSearch.search(info.name, info.enumName, selector).findAll()
		if(result.isEmpty()) return
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
	
	override fun doInvoke(element: ParadoxScriptStringExpressionElement, info: ParadoxComplexEnumValueInfo, config: CwtComplexEnumConfig, editor: Editor, project: Project) {
		val enumName = info.enumName
		val resolved = config.pointer.element ?: return
		NavigationUtil.getPsiElementPopup(arrayOf(resolved), PlsBundle.message("script.intention.complexEnumValueName.gotoTypeDeclaration.title", enumName))
			.showInBestPositionFor(editor)
	}
}
