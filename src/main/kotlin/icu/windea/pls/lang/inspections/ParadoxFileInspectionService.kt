package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.PsiBuilderUtil.*
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.vfs.VirtualFileBomService
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.fixes.ChangeFileEncodingFix
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.psi.ParadoxFile
import icu.windea.pls.lang.util.ParadoxFileEncodingManager
import icu.windea.pls.model.constants.PlsConstants
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
        val hasBom = VirtualFileBomService.hasBom(virtualFile, PlsConstants.utf8Bom)
        if (isValidCharset && hasBom) return null

        val expect = expectedCharset.displayName() + if (useBom) " BOM" else " NO BOM"
        val actual = charset.displayName() + if (hasBom) " BOM" else " NO BOM"

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("incorrectFileEncoding.desc", actual, expect)
        val fix = ChangeFileEncodingFix(file, expectedCharset, true)
        holder.registerProblem(file, description, fix)
        return holder.resultsArray
    }

    fun checkFileMatched(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is ParadoxFile) return null
        val virtualFile = file.virtualFile ?: return null
        val fileInfo = virtualFile.fileInfo ?: return null // 无法获取文件信息时跳过检查

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
}
