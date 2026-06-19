package icu.windea.pls.lang.inspections

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.InspectionManager
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
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.matchesAntPatterns
import icu.windea.pls.core.vfs.VirtualFileBomService
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.fixes.ChangeFileEncodingFix
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.psi.ParadoxFile
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.ParadoxFileEncodingManager
import icu.windea.pls.lang.util.ParadoxLocalisationFileManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.script.psi.ParadoxScriptFile

object ParadoxFileInspectionService {
    fun checkFileEncoding(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is ParadoxFile) return null
        val virtualFile = file.virtualFile ?: return null
        val fileInfo = virtualFile.fileInfo ?: return null // 无法获取文件信息时跳过检查

        val expectedCharset = ParadoxFileEncodingManager.useCharset()
        val charset = virtualFile.charset
        val isValidCharset = charset == expectedCharset
        val useBom = ParadoxFileEncodingManager.useBom(file, fileInfo)
        val hasBom = VirtualFileBomService.hasBom(virtualFile, VirtualFileBomService.utf8Bom)
        val isValidBom = useBom == hasBom
        if (isValidCharset && isValidBom) return null

        val expect = expectedCharset.displayName() + if (useBom) " BOM" else " NO BOM"
        val actual = charset.displayName() + if (hasBom) " BOM" else " NO BOM"

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("incorrectFileEncoding.desc", actual, expect)
        val fix = ChangeFileEncodingFix(file, expectedCharset, true)
        holder.registerProblem(file, description, fix)
        return holder.resultsArray
    }

    fun checkFileName(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, ignoredFilePaths: String): Array<ProblemDescriptor>? {
        if (file !is ParadoxLocalisationFile) return null
        val virtualFile = file.virtualFile ?: return null
        val fileInfo = virtualFile.fileInfo ?: return null // 无法获取文件信息时跳过检查

        // 排除忽略的文件
        if (fileInfo.path.path.matchesAntPatterns(ignoredFilePaths, ignoreCase = true)) return null // 忽略

        // 仅对于存在且仅存在一个locale的本地化文件
        val singlePropertyList = file.children().filterIsInstance<ParadoxLocalisationPropertyList>().singleOrNull() ?: return null
        val locale = singlePropertyList.locale ?: return null
        if (!locale.isValid) return null // locale尚未填写完成时也跳过检查
        val localeConfig = selectLocale(locale) ?: return null // locale不支持时也跳过检查
        val localeId = localeConfig.id
        val localeIdFromFile = ParadoxLocalisationFileManager.getLocaleIdFromFileName(file)
        if (localeIdFromFile == localeId) return null // 匹配语言环境，跳过
        val expectedFileName = ParadoxLocalisationFileManager.getExpectedFileName(file, localeId)

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val location = locale // 不要直接注册到文件上
        val description = PlsBundle.message("incorrectFileName.desc", file.name, localeId)
        val fixes = getFileNameFixes(locale, expectedFileName, localeIdFromFile)
        holder.registerProblem(location, description, *fixes)
        return holder.resultsArray
    }

    fun checkFileMatched(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, ignoredFilePaths: String): Array<ProblemDescriptor>? {
        if (file !is ParadoxFile) return null
        val virtualFile = file.virtualFile ?: return null
        val fileInfo = virtualFile.fileInfo ?: return null // 无法获取文件信息时跳过检查

        // 排除忽略的文件
        if (fileInfo.path.path.matchesAntPatterns(ignoredFilePaths, ignoreCase = true)) return null // 忽略

        val gameType = fileInfo.rootInfo.gameType
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val matched = ParadoxConfigMatchService.isMatchedOnFileLevel(file, configGroup, fileInfo.path)
        if (matched) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = when {
            file is ParadoxScriptFile -> PlsBundle.message("unmatchedFile.desc.script")
            file is ParadoxCsvFile -> PlsBundle.message("unmatchedFile.desc.csv")
            else -> return null
        }
        holder.registerProblem(file, description)
        return holder.resultsArray
    }

    // region Fixes

    private fun getFileNameFixes(locale: ParadoxLocalisationLocale, expectedFileName: String, localeIdFromFile: String?): Array<LocalQuickFix> {
        return buildList {
            this += RenameFileFix(locale, expectedFileName)
            if (localeIdFromFile != null) this += RenameLocaleFix(locale, localeIdFromFile)
        }.toTypedArray()
    }

    // org.jetbrains.kotlin.idea.intentions.RenameFileToMatchClassIntention

    private class RenameFileFix(
        element: ParadoxLocalisationLocale,
        private val expectedFileName: String
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
        override fun getText() = PlsBundle.message("incorrectFileName.fix.1.name", expectedFileName)

        override fun getFamilyName() = PlsBundle.message("incorrectFileName.fix.1.familyName")

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
        override fun getPriority() = PriorityAction.Priority.TOP // 高优先级，如果可用

        override fun getText() = PlsBundle.message("incorrectFileName.fix.2.name", expectedLocaleId)

        override fun getFamilyName() = PlsBundle.message("incorrectFileName.fix.2.familyName")

        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            val locale = startElement.castOrNull<ParadoxLocalisationLocale>() ?: return
            locale.name = expectedLocaleId
        }
    }

    // endregion
}
