package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.psi.PsiFileOnlyVisitor
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.inspections.ParadoxFileInspectionContext
import icu.windea.pls.lang.inspections.ParadoxFileInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.model.constraints.ParadoxPathConstraint

/**
 * 检查当前文件是否使用了正确的文件名。
 *
 * 说明：
 * - 忽略注入的文件和临时文件。
 * - 仅检查普通的本地化文件（位于 `localisation` 或 `localization` 目录下）。
 *
 * 提供快速修复：
 * - 改为正确的文件名
 * - 改为正确的语言环境名
 *
 * @property ignoredFilePaths （配置项）需要忽略的文件路径。一组 ANT 路径模式，分号分隔，忽略大小写。
 */
class IncorrectFileNameInspection : LocalInspectionTool(), DumbAware {
    @JvmField var ignoredFilePaths = "**/languages.yml"

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.expandableString("ignoredFilePaths", ChronicleInspectionBundle.message("option.ignoredFilePaths"), ",")
                .description(ChronicleBundle.message("comment.antPatterns"))
        )
    }

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 跳过内存文件和注入的文件
        val vFile = file.virtualFile
        if (VirtualFileService.isLightFile(vFile)) return false
        if (VirtualFileService.isInjectedFile(vFile)) return false
        // 要求是语义上有效的本地化文件（仅限普通的本地化文件）
        return ParadoxPsiFileMatchService.isLocalisationFile(file, ParadoxPathConstraint.ForNormalLocalisation)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val context = createContext(holder)
        return object : PsiFileOnlyVisitor() {
            override fun visitFile(file: PsiFile) {
                ProgressManager.checkCanceled()
                ParadoxFileInspectionService.checkForIncorrectFileName(file, context)
            }
        }
    }

    private fun createContext(holder: ProblemsHolder): ParadoxFileInspectionContext {
        return ParadoxFileInspectionContext(this, holder, ignoredFilePaths)
    }
}
