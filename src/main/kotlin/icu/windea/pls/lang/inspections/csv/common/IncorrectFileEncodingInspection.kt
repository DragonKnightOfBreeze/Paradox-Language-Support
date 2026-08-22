package icu.windea.pls.lang.inspections.csv.common

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
import icu.windea.pls.lang.inspections.ParadoxFileInspectionContext
import icu.windea.pls.lang.inspections.ParadoxFileInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService

// com.intellij.openapi.editor.actions.AddBomAction
// com.intellij.openapi.editor.actions.RemoveBomAction

/**
 * 检查当前 CSV 文件是否使用了正确的文件编码。
 *
 * 说明：
 * - 忽略注入的文件和临时文件。
 * - 忽略空文件。
 *
 * 提供快速修复：
 * - 改为正确的文件编码
 *
 * @property ignoredFilePaths （配置项）需要忽略的文件路径。一组 ANT 路径模式，分号分隔，忽略大小写。
 *
 * @see icu.windea.pls.lang.ParadoxUtf8BomOptionProvider
 */
class IncorrectFileEncodingInspection : LocalInspectionTool(), DumbAware, ParadoxFileInspectionContext.Aware {
    @JvmField var ignoredFilePaths = ""

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.expandableString("ignoredFilePaths", ChronicleBundle.message("inspection.option.ignoredFilePaths"), ",")
                .description(ChronicleBundle.message("comment.antPatterns"))
        )
    }

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 跳过内存文件和注入的文件
        val vFile = file.virtualFile
        if (VirtualFileService.isLightFile(vFile)) return false
        if (VirtualFileService.isInjectedFile(vFile)) return false
        // 要求是语义上有效的 CSV 文件
        return ParadoxPsiFileMatchService.isCsvFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val context = ParadoxFileInspectionContext(this, holder, ignoredFilePaths)
        return object : PsiFileOnlyVisitor() {
            override fun visitFile(file: PsiFile) {
                ProgressManager.checkCanceled()
                ParadoxFileInspectionService.checkForIncorrectFileEncoding(file, context)
            }
        }
    }
}
