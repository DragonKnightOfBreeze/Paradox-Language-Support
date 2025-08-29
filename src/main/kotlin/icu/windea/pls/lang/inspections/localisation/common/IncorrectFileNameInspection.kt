package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringSettings
import com.intellij.refactoring.rename.RenameProcessor
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.ParadoxLocalisationFileManager
import icu.windea.pls.lang.util.PlsVfsManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.model.paths.ParadoxPathMatcher
import icu.windea.pls.model.paths.matches

/**
 * 不正确的文件名的检查。
 *
 * 提供快速修复：
 * * 改为正确的文件名
 * * 改为正确的语言区域名
 */
class IncorrectFileNameInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (PlsVfsManager.isLightFile(file.virtualFile)) return false //不检查临时文件
        val fileInfo = file.fileInfo ?: return false
        return fileInfo.path.matches(ParadoxPathMatcher.InLocalisationPath)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is ParadoxLocalisationFile) return null

        //仅对于存在且仅存在一个locale的本地化文件
        var theOnlyPropertyList: ParadoxLocalisationPropertyList? = null
        file.children().filterIsInstance<ParadoxLocalisationPropertyList>().process {
            if (theOnlyPropertyList == null) {
                theOnlyPropertyList = it
                true
            } else {
                false
            }
        }
        val locale = theOnlyPropertyList?.locale ?: return null
        if (!locale.isValid) return null //locale尚未填写完成时也跳过检查
        val localeConfig = selectLocale(locale) ?: return null //locale不支持时也跳过检查
        val localeId = localeConfig.id
        val fileName = file.name
        val localeIdFromFile = ParadoxLocalisationFileManager.getLocaleIdFromFileName(file)
        if (localeIdFromFile == localeId) return null //匹配语言区域，跳过
        val expectedFileName = ParadoxLocalisationFileManager.getExpectedFileName(file, localeId)
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val quickFixes = buildList {
            this += RenameFileFix(locale, expectedFileName)
            if (localeIdFromFile != null) this += RenameLocaleFix(locale, localeIdFromFile)
        }.toTypedArray<LocalQuickFix>()
        //将检查注册在locale上，而非file上
        holder.registerProblem(locale, PlsBundle.message("inspection.localisation.incorrectFileName.desc", fileName, localeId), *quickFixes)
        return holder.resultsArray
    }

    //org.jetbrains.kotlin.idea.intentions.RenameFileToMatchClassIntention

    private class RenameFileFix(
        element: ParadoxLocalisationLocale,
        private val expectedFileName: String
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
        override fun getText() = PlsBundle.message("inspection.localisation.incorrectFileName.fix.1", expectedFileName)

        override fun getFamilyName() = text

        override fun getPriority() = PriorityAction.Priority.HIGH

        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            RenameProcessor(
                project,
                file,
                expectedFileName,
                RefactoringSettings.getInstance().RENAME_SEARCH_IN_COMMENTS_FOR_FILE,
                RefactoringSettings.getInstance().RENAME_SEARCH_FOR_TEXT_FOR_FILE
            ).run()
        }

        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY

        override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

        override fun startInWriteAction() = false
    }

    private class RenameLocaleFix(
        element: ParadoxLocalisationLocale,
        private val expectedLocaleId: String
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
        override fun getPriority() = PriorityAction.Priority.TOP //高优先级，如果可用

        override fun getText() = PlsBundle.message("inspection.localisation.incorrectFileName.fix.2", expectedLocaleId)

        override fun getFamilyName() = text

        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            val locale = startElement.castOrNull<ParadoxLocalisationLocale>() ?: return
            locale.name = expectedLocaleId
        }
    }
}
