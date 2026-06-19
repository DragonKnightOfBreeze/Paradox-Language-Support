package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.vfs.VirtualFileBomService
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.fixes.ChangeFileEncodingFix
import icu.windea.pls.lang.util.ParadoxFileEncodingManager
import icu.windea.pls.model.constants.PlsConstants

object ParadoxFileInspectionService {
    fun checkFileEncoding(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val virtualFile = file.virtualFile ?: return null
        val fileInfo = virtualFile.fileInfo ?: return null // 无法获取文件信息时跳过检查

        val expectedCharset = ParadoxFileEncodingManager.useCharset()
        val charset = virtualFile.charset
        val isValidCharset = charset == expectedCharset
        val useBom = ParadoxFileEncodingManager.useBom(fileInfo)
        val hasBom = VirtualFileBomService.hasBom(virtualFile, PlsConstants.utf8Bom)
        if (isValidCharset && hasBom) return null

        val expect = expectedCharset.displayName() + if(useBom) " BOM" else " NO BOM"
        val actual = charset.displayName() + if(hasBom) " BOM" else " NO BOM"

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("incorrectFileEncoding.desc", actual, expect)
        val fix = ChangeFileEncodingFix(file, Charsets.UTF_8, true)
        holder.registerProblem(file, description, fix)
        return holder.resultsArray
    }
}
