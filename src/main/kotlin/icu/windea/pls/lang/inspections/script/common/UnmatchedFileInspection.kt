package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.config.CwtFilePathMatchableConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.core.psi.PsiFileOnlyVisitor
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.inspections.ParadoxFileInspectionContext
import icu.windea.pls.lang.inspections.ParadoxFileInspectionService
import icu.windea.pls.lang.inspections.script.inlineScript.InlineScriptInspectionBase
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService

/**
 * 检查当前脚本文件是否无法匹配任何规则（包括：类型规则、复杂枚举规则）。
 *
 * 说明：
 * - 忽略注入的文件和临时文件。
 * - 忽略直接位于游戏或入口目录下的文件。
 * - 忽略用于声明定值、全局封装变量或内联脚本的文件。它们分别位于 `common/defines` `common/scripted_variables` 和 `common/inline_scripts` 目录下。
 *
 * @property ignoredFilePaths （配置项）需要忽略的文件路径。一组 ANT 路径模式，分号分隔，忽略大小写。
 *
 * @see CwtFilePathMatchableConfig
 * @see CwtTypeConfig
 * @see CwtComplexEnumConfig
 */
class UnmatchedFileInspection : InlineScriptInspectionBase() {
    @JvmField var ignoredFilePaths = ""

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
        // 忽略直接位于游戏或入口目录下的文件
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val context = createContext(holder)
        return object : PsiFileOnlyVisitor() {
            override fun visitFile(file: PsiFile) {
                ProgressManager.checkCanceled()
                ParadoxFileInspectionService.checkForUnmatchedFile(file, context)
            }
        }
    }

    private fun createContext(holder: ProblemsHolder): ParadoxFileInspectionContext {
        return ParadoxFileInspectionContext(this, holder, ignoredFilePaths)
    }
}
