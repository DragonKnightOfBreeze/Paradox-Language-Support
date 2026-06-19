package icu.windea.pls.lang.inspections.csv.common

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.fixes.GotoInlineScriptUsagesFix
import icu.windea.pls.lang.inspections.script.inlineScript.InlineScriptInspectionBase
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.config.config.CwtFilePathMatchableConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ParadoxFileInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher

/**
 * 检查当前脚本文件是否无法匹配任何规则（包括：行规则）。
 *
 * 说明：
 * - 忽略注入的文件和临时文件。
 * - 忽略直接位于游戏或入口目录下的文件。
 *
 * @see CwtFilePathMatchableConfig
 * @see CwtRowConfig
 */
class UnmatchedFileInspection : InlineScriptInspectionBase() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 跳过内存文件和注入的文件
        val virtualFile = file.virtualFile
        if (VirtualFileService.isLightFile(virtualFile)) return false
        if (VirtualFileService.isInjectedFile(virtualFile)) return false
        // 忽略直接位于游戏或入口目录下的文件
        if (ParadoxPsiFileMatcher.isTopFileFromRoot(file)) return false
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是可接受的 CSV 文件
        return ParadoxPsiFileMatcher.isCsvFile(file)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        return ParadoxFileInspectionService.checkFileMatched(file, manager, isOnTheFly)
    }
}
