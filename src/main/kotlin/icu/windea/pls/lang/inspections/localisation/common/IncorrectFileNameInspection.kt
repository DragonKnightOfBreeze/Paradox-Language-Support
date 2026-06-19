package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.toCommaDelimitedString
import icu.windea.pls.core.toCommaDelimitedStringList
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ParadoxFileInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import javax.swing.JComponent

/**
 * 检查当前本地化文件是否使用了正确的文件名。
 *
 * 说明：
 * - 忽略注入的文件和临时文件。
 *
 * 提供快速修复：
 * - 改为正确的文件名
 * - 改为正确的语言环境名
 *
 * @property ignoredFilePaths （配置项）需要忽略的文件路径。一组 ANT 路径模式，分号分隔，忽略大小写。
 */
class IncorrectFileNameInspection : LocalInspectionTool(), DumbAware {
    @JvmField
    var ignoredFilePaths = "**/languages.yml"

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 跳过内存文件和注入的文件
        val virtualFile = file.virtualFile
        if (VirtualFileService.isLightFile(virtualFile)) return false
        if (VirtualFileService.isInjectedFile(virtualFile)) return false
        // 要求是可接受的本地化文件
        return ParadoxPsiFileMatcher.isLocalisationFile(file)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        return ParadoxFileInspectionService.checkFileName(file, manager, isOnTheFly, ignoredFilePaths)
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredFilePaths
            row {
                label(PlsBundle.message("incorrectFileName.option.ignoredFilePaths"))
                expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                    .bindText(::ignoredFilePaths.toAtomicProperty())
                    .comment(PlsBundle.message("comment.antPatterns"))
                    .align(Align.FILL)
                    .resizableColumn()
            }
        }
    }
}
