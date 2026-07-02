package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ParadoxFileInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService

// com.intellij.openapi.editor.actions.AddBomAction
// com.intellij.openapi.editor.actions.RemoveBomAction

/**
 * 检查当前本地化文件是否使用了正确的文件编码。
 *
 * 说明：
 * - 忽略注入的文件和临时文件。
 * - 忽略空文件。
 *
 * 提供快速修复：
 * - 改为正确的文件编码
 *
 * @see icu.windea.pls.lang.ParadoxUtf8BomOptionProvider
 */
class IncorrectFileEncodingInspection : LocalInspectionTool(), DumbAware {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 跳过内存文件和注入的文件
        val virtualFile = file.virtualFile
        if (VirtualFileService.isLightFile(virtualFile)) return false
        if (VirtualFileService.isInjectedFile(virtualFile)) return false
        // 要求是可接受的本地化文件
        return ParadoxPsiFileMatchService.isLocalisationFile(file)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        return ParadoxFileInspectionService.checkFileEncoding(file, manager, isOnTheFly)
    }
}
