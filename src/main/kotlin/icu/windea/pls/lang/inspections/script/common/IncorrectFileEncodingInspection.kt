package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.hasBom
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.quickfix.ChangeFileEncodingFix
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.vfs.PlsVfsManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsConstants

// com.intellij.openapi.editor.actions.AddBomAction
// com.intellij.openapi.editor.actions.RemoveBomAction

/**
 * 不正确的的文件编码的检查。
 *
 * 提供快速修复：
 * - 改为正确的文件编码
 *
 * @see icu.windea.pls.lang.ParadoxUtf8BomOptionProvider
 */
class IncorrectFileEncodingInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (PlsVfsManager.isLightFile(file.virtualFile)) return false // 不检查临时文件
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<out ProblemDescriptor>? {
        val virtualFile = file.virtualFile ?: return null
        val fileInfo = virtualFile.fileInfo ?: return null // 无法获取文件信息时跳过检查
        val charset = virtualFile.charset
        val hasBom = virtualFile.hasBom(PlsConstants.utf8Bom)
        val isValidCharset = charset == Charsets.UTF_8
        val validBom = run {
            val gameType = selectGameType(virtualFile)
            when {
                // name list file -> BOM, others -> NO BOM
                gameType == ParadoxGameType.Stellaris -> fileInfo.path.parent.startsWith("common/name_lists")
                // all -> unspecified
                else -> null
            }
        }
        val isValidBom = if (validBom != null) hasBom == validBom else true
        if (isValidCharset && isValidBom) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val expect = "UTF-8" + if (validBom == null) "" else if (validBom) " BOM" else " NO BOM"
        val actual = "UTF-8" + if (hasBom) " BOM" else " NO BOM"
        val message = PlsBundle.message("inspection.script.incorrectFileEncoding.desc.1", actual, expect)
        val fix = ChangeFileEncodingFix(file, Charsets.UTF_8, validBom)
        holder.registerProblem(file, message, fix)
        return holder.resultsArray
    }
}
