package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.vfs.VirtualFileBomService
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.ParadoxUtf8BomOptionProvider
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.fixes.ChangeFileEncodingFix
import icu.windea.pls.lang.inspections.ParadoxFileInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.util.ParadoxFileEncodingManager
import icu.windea.pls.model.constants.PlsConstants

// com.intellij.openapi.editor.actions.AddBomAction
// com.intellij.openapi.editor.actions.RemoveBomAction

/**
 * 检查当前脚本文件是否使用了正确的文件编码。
 *
 * 说明：
 * - 忽略注入的文件和临时文件。
 *
 * 提供快速修复：
 * - 改为正确的文件编码
 *
 * @see ParadoxUtf8BomOptionProvider
 */
class IncorrectFileEncodingInspection : LocalInspectionTool(), DumbAware {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 跳过内存文件和注入的文件
        val virtualFile = file.virtualFile
        if (VirtualFileService.isLightFile(virtualFile)) return false
        if (VirtualFileService.isInjectedFile(virtualFile)) return false
        // 要求是可接受的脚本文件
        return ParadoxPsiFileMatcher.isScriptFile(file)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        return ParadoxFileInspectionService.checkFileEncoding(file, manager, isOnTheFly)
    }
}
