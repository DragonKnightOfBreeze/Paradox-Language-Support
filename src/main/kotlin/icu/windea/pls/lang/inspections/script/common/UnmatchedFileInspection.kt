package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtFilePathMatchableConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.toCommaDelimitedString
import icu.windea.pls.core.toCommaDelimitedStringList
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ParadoxFileInspectionService
import icu.windea.pls.lang.inspections.script.inlineScript.InlineScriptInspectionBase
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import javax.swing.JComponent

/**
 * 检查当前脚本文件是否无法匹配任何规则（包括：类型规则、复杂枚举规则）。
 *
 * 说明：
 * - 忽略注入的文件和临时文件。
 * - 忽略直接位于游戏或入口目录下的文件。
 *
 * @property ignoredFilePaths （配置项）需要忽略的文件路径。一组 ANT 路径模式，分号分隔，忽略大小写。
 *
 * @see CwtFilePathMatchableConfig
 * @see CwtTypeConfig
 * @see CwtComplexEnumConfig
 */
class UnmatchedFileInspection : InlineScriptInspectionBase() {
    @JvmField
    var ignoredFilePaths = "common/inline_scripts/**"

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 跳过内存文件和注入的文件
        val virtualFile = file.virtualFile
        if (VirtualFileService.isLightFile(virtualFile)) return false
        if (VirtualFileService.isInjectedFile(virtualFile)) return false
        // 忽略直接位于游戏或入口目录下的文件
        if (ParadoxPsiFileMatcher.isTopFileFromRoot(file)) return false
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是可接受的脚本文件
        return ParadoxPsiFileMatcher.isScriptFile(file)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        return ParadoxFileInspectionService.checkFileMatched(file, manager, isOnTheFly, ignoredFilePaths)
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredFilePaths
            row {
                label(PlsBundle.message("unmatchedFile.option.ignoredFilePaths"))
            }
            row {
                expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                    .bindText(::ignoredFilePaths.toAtomicProperty())
                    .comment(PlsBundle.message("comment.antPatterns"))
                    .align(Align.FILL)
                    .resizableColumn()
            }
        }
    }
}
