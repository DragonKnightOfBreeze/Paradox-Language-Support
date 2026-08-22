package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor

/**
 * 检查是否存在无法解析的文本颜色引用。
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedColorInspection : LocalInspectionTool() {
    @JvmField var ignoredInInjectedFiles = false

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("ignoredInInjectedFiles", ChronicleInspectionBundle.message("option.ignoredInInjectedFiles"))
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
            override fun visitColorfulText(element: ParadoxLocalisationColorfulText) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    private fun check(element: ParadoxLocalisationColorfulText, holder: ProblemsHolder) {
        val name = element.name ?: return
        val reference = element.reference
        if (reference == null || reference.resolve() != null) return
        val location = element.idElement ?: return
        val description = ChronicleInspectionBundle.message("localisation.unresolvedColor.desc", name)
        holder.registerProblem(location, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
    }
}
