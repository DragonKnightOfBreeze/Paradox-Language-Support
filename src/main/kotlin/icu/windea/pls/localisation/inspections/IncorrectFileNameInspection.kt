package icu.windea.pls.localisation.inspections

import com.intellij.codeInsight.intention.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.refactoring.*
import com.intellij.refactoring.rename.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

/**
 * 不正确的文件名的检查。
 *
 * 提供快速修复：
 * * 重命名文件名（如果以下快速修复不可用）
 * * 改为正确的文件名
 * * 改为正确的语言区域名
 */
class IncorrectFileNameInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxLocalisationFile) return null //不应该出现
		//仅对于存在且仅存在一个locale的本地化文件
		var theOnlyPropertyList: ParadoxLocalisationPropertyList? = null
		file.processChildrenOfType<ParadoxLocalisationPropertyList> {
			if(theOnlyPropertyList == null) {
				theOnlyPropertyList = it
				true
			} else {
				return null
			}
		}
		val locale = theOnlyPropertyList?.locale ?: return null
		if(!locale.isValid) return null //locale尚未填写完成时也跳过检查
		val localeConfig = locale.localeConfig ?: return null //locale不支持时也跳过检查
		val fileName = file.name
		val localeIdFromFile = file.getLocaleIdFromFileName()
		val expectedFileName = file.getExpectedFileName(localeConfig.id)
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		val quickFixList = SmartList<LocalQuickFix>()
		quickFixList.add(RenameFileFix(locale, expectedFileName))
		if(localeIdFromFile != null) quickFixList.add(RenameLocaleFix(locale, localeIdFromFile))
		val quickFixes = quickFixList.toTypedArray()
		//将检查注册在locale上，而非file上
		holder.registerProblem(locale, PlsBundle.message("localisation.inspection.incorrectFileName.description", fileName, localeConfig.id), *quickFixes)
		return holder.resultsArray
	}
	
	//org.jetbrains.kotlin.idea.intentions.RenameFileToMatchClassIntention
	
	private class RenameFileFix(
		element: ParadoxLocalisationLocale,
		private val expectedFileName: String
	) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
		override fun getFamilyName() = PlsBundle.message("localisation.inspection.incorrectFileName.fix.1", expectedFileName)
		override fun getText() = PlsBundle.message("localisation.inspection.incorrectFileName.fix.1", expectedFileName)
		
		override fun startInWriteAction() = false
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			RenameProcessor(
				project,
				file,
				expectedFileName,
				RefactoringSettings.getInstance().RENAME_SEARCH_IN_COMMENTS_FOR_FILE,
				RefactoringSettings.getInstance().RENAME_SEARCH_FOR_TEXT_FOR_FILE
			).run()
		}
	}
	
	private class RenameLocaleFix(
		element: ParadoxLocalisationLocale,
		private val expectedLocaleId: String
	) : LocalQuickFixAndIntentionActionOnPsiElement(element), HighPriorityAction {
		override fun getFamilyName() = PlsBundle.message("localisation.inspection.incorrectFileName.fix.2", expectedLocaleId)
		override fun getText() = PlsBundle.message("localisation.inspection.incorrectFileName.fix.2", expectedLocaleId)
		
		override fun getPriority() = PriorityAction.Priority.TOP //高优先级，如果可用
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			val locale = startElement.castOrNull<ParadoxLocalisationLocale>() ?: return
			locale.name = expectedLocaleId
		}
	}
	
	//com.intellij.codeInsight.daemon.impl.quickfix.RenameFileFix
	
	//private class RenameFileFix(
	//	element: ParadoxLocalisationLocale
	//) : LocalQuickFixAndIntentionActionOnPsiElement(element), HighPriorityAction {
	//	override fun getFamilyName() = PlsBundle.message("localisation.inspection.incorrectFileName.fix.3")
	//	
	//	override fun getText() = PlsBundle.message("localisation.inspection.incorrectFileName.fix.3")
	//	
	//	override fun getPriority() = PriorityAction.Priority.NORMAL
	//	
	//	override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
	//		PsiElementRenameHandler.invoke(file, project, file, editor) //不限制更改文件扩展名
	//	}
	//}
}