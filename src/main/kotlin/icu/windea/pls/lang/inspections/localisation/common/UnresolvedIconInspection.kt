package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.core.matchesPatterns
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor

/**
 * 无法解析的图标的代码检查。
 *
 * @property ignoredNames （配置项）需要忽略的名字。一组模式，分号分隔，忽略大小写。
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedIconInspection : LocalInspectionTool() {
    @JvmField var ignoredNames = ""
    @JvmField var ignoredInInjectedFiles = false

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("ignoredNames", ChronicleInspectionBundle.message("localisation.unresolvedIcon.option.ignoredNames")),
            OptPane.checkbox("ignoredInInjectedFiles", ChronicleInspectionBundle.message("option.ignoredInInjectedFiles")),
        )
    }

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 按需忽略注入的文件
        val vFile = file.virtualFile
        if (ignoredInInjectedFiles && VirtualFileService.isInjectedFile(vFile)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的本地化文件
        return ParadoxPsiFileMatchService.isLocalisationFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxLocalisationVisitor() {
            override fun visitIcon(element: ParadoxLocalisationIcon) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    private fun check(element: ParadoxLocalisationIcon, holder: ProblemsHolder) {
        val name = element.name ?: return
        if (skip(name)) return // 忽略
        val reference = element.reference
        if (reference == null || reference.resolve() != null) return
        val location = element.idElement ?: return
        val description = ChronicleInspectionBundle.message("localisation.unresolvedIcon.desc", name)
        holder.registerProblem(location, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
    }

    private fun skip(name: String): Boolean {
        if (ignoredNames.isNotEmpty() && name.matchesPatterns(ignoredNames, ignoreCase = true)) return true
        return false
    }
}
