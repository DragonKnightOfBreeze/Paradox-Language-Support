package icu.windea.pls.lang.inspections

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringSettings
import com.intellij.refactoring.rename.RenameProcessor
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.fixes.BrowseUrlFix
import icu.windea.pls.core.matchesAntPatterns
import icu.windea.pls.core.vfs.VirtualFileBomService
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.analysis.ParadoxFileEncodingService
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.fixes.ChangeFileEncodingFix
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.psi.ParadoxFile
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.ParadoxLocalisationFileManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.model.constants.ChronicleUrls
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.model.constraints.matchesBy
import icu.windea.pls.script.psi.ParadoxScriptFile

object ParadoxFileInspectionService {
    // region IncorrectFileEncodingInspection

    fun checkForIncorrectFileEncoding(file: PsiFile, context: ParadoxFileInspectionContext) {
        val holder = context.holder
        if (!file.isValid) return
        if (file.textLength == 0) return // 2.2.0 lenient check (skip for empty files)
        if (file !is ParadoxFile) return

        val virtualFile = file.virtualFile ?: return
        if (VirtualFileService.isStubFile(virtualFile)) return
        if (!virtualFile.isValid) return
        if (virtualFile.length == 0L) return // 2.2.0 lenient check (skip for empty files)
        val fileInfo = virtualFile.fileInfo ?: return // 无法获取文件信息时跳过检查

        val expectedCharset = ParadoxFileEncodingService.useCharset()
        val charset = virtualFile.charset
        val isValidCharset = charset == expectedCharset
        val useBom = ParadoxFileEncodingService.useBom(file, fileInfo)
        val hasBom = VirtualFileBomService.hasBom(virtualFile, VirtualFileBomService.utf8Bom)
        val isValidBom = useBom == null || useBom == hasBom
        if (isValidCharset && isValidBom) return

        val expect = expectedCharset.displayName() + if (useBom == null) "" else if (useBom) " BOM" else " NO BOM"
        val actual = charset.displayName() + if (hasBom) " BOM" else " NO BOM"

        val description = ChronicleInspectionBundle.message("lang.incorrectFileEncoding.desc", actual, expect)
        val fix = ChangeFileEncodingFix(file, expectedCharset, useBom)
        holder.registerProblem(file, description, fix)
    }

    // endregion

    // region IncorrectFileNameInspection

    fun checkForIncorrectFileName(file: PsiFile, context: ParadoxFileInspectionContext) {
        val holder = context.holder
        if (!file.isValid) return
        if (file !is ParadoxLocalisationFile) return

        val virtualFile = file.virtualFile ?: return
        if (VirtualFileService.isStubFile(virtualFile)) return
        if (!virtualFile.isValid) return
        val fileInfo = virtualFile.fileInfo ?: return // 无法获取文件信息时跳过检查

        // 排除忽略的文件
        if (fileInfo.path.path.matchesAntPatterns(context.ignoredFilePaths, ignoreCase = true)) return // 忽略

        // 仅对于存在且仅存在一个locale的本地化文件
        val singlePropertyList = file.children().filterIsInstance<ParadoxLocalisationPropertyList>().singleOrNull() ?: return
        val locale = singlePropertyList.locale ?: return
        if (!locale.isValid) return // locale尚未填写完成时也跳过检查
        val localeConfig = selectLocale(locale) ?: return // locale不支持时也跳过检查
        val localeId = localeConfig.name
        val localeIdFromFile = ParadoxLocalisationFileManager.getLocaleIdFromFileName(file)
        if (localeIdFromFile == localeId) return // 匹配语言环境，跳过
        val expectedFileName = ParadoxLocalisationFileManager.getExpectedFileName(file, localeId)

        val location = locale // 不要直接注册到文件上
        val description = ChronicleInspectionBundle.message("lang.incorrectFileName.desc", file.name, localeId)
        val fixes = buildList {
            this += RenameFileFix(locale, expectedFileName)
            if (localeIdFromFile != null) this += RenameLocaleFix(locale, localeIdFromFile)
        }.toArray(LocalQuickFix.EMPTY_ARRAY)
        holder.registerProblem(location, description, *fixes)
    }

    // org.jetbrains.kotlin.idea.intentions.RenameFileToMatchClassIntention

    private class RenameFileFix(
        element: ParadoxLocalisationLocale,
        private val expectedFileName: String
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
        override fun getText() = ChronicleInspectionBundle.message("lang.incorrectFileName.fix.1.name", expectedFileName)

        override fun getFamilyName() = ChronicleInspectionBundle.message("lang.incorrectFileName.fix.1.familyName")

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
        override fun getText() = ChronicleInspectionBundle.message("lang.incorrectFileName.fix.2.name", expectedLocaleId)

        override fun getFamilyName() = ChronicleInspectionBundle.message("lang.incorrectFileName.fix.2.familyName")

        override fun getPriority() = PriorityAction.Priority.TOP // 最高优先级，如果可用

        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            val locale = startElement.castOrNull<ParadoxLocalisationLocale>() ?: return
            locale.name = expectedLocaleId
        }
    }

    // endregion

    // region UnmatchedFileInspection

    fun checkForUnmatchedFile(file: PsiFile, context: ParadoxFileInspectionContext) {
        val holder = context.holder
        if (file !is ParadoxFile) return
        val virtualFile = file.virtualFile ?: return
        val fileInfo = virtualFile.fileInfo ?: return // 无法获取文件信息时跳过检查

        // 忽略一些特殊的脚本文件
        if (file is ParadoxScriptFile && fileInfo.path matchesBy ParadoxPathConstraint.SpecialScriptFile) return

        // 排除忽略的文件
        if (fileInfo.path.path.matchesAntPatterns(context.ignoredFilePaths, ignoreCase = true)) return // 忽略

        val gameType = fileInfo.gameType
        val configGroup = ChronicleFacade.getConfigGroup(file.project, gameType)
        val matched = ParadoxConfigMatchService.isMatchedOnFileLevel(file, configGroup, fileInfo.path)
        if (matched) return

        val description = when {
            file is ParadoxScriptFile -> ChronicleInspectionBundle.message("lang.unmatchedFile.desc.script")
            file is ParadoxCsvFile -> ChronicleInspectionBundle.message("lang.unmatchedFile.desc.csv")
            else -> return
        }
        val fixes = arrayOf(
            BrowseUrlFix(ChronicleInspectionBundle.message("lang.unmatchedFile.fix.1"), ChronicleUrls.contributing),
            BrowseUrlFix(ChronicleInspectionBundle.message("lang.unmatchedFile.fix.2"), ChronicleUrls.configRepositories),
        )
        holder.registerProblem(file, description, *fixes)
    }

    // endregion
}
