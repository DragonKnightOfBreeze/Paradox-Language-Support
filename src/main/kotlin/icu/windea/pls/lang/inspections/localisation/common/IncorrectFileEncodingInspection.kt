package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.hasBom
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.quickfix.ChangeFileEncodingFix
import icu.windea.pls.lang.util.PlsVfsManager
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.paths.ParadoxPathMatcher
import icu.windea.pls.model.paths.matches

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
        val fileInfo = file.fileInfo ?: return false
        return fileInfo.path.matches(ParadoxPathMatcher.InLocalisationPath)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<out ProblemDescriptor>? {
        val virtualFile = file.virtualFile ?: return null
        val charset = virtualFile.charset
        val hasBom = virtualFile.hasBom(PlsConstants.utf8Bom)
        val isValidCharset = charset == Charsets.UTF_8
        val isValidBom = hasBom
        if (isValidCharset && isValidBom) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val expect = "UTF-8 BOM"
        val actual = "UTF-8" + if (hasBom) "BOM" else "NO BOM"
        val message = PlsBundle.message("inspection.localisation.incorrectFileEncoding.desc.1", actual, expect)
        val fix = ChangeFileEncodingFix(file, Charsets.UTF_8, true)
        holder.registerProblem(file, message, fix)
        return holder.resultsArray
    }
}
