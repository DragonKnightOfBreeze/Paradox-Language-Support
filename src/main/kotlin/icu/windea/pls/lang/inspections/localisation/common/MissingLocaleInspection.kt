package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.matchesPatterns
import icu.windea.pls.core.psi.PsiFileOnlyVisitor
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile

/**
 * 检查本地化文件中是否缺少语言环境声明。
 *
 * @property ignoredFileNames （配置项）需要忽略检查的文件名。一组模式，分号分隔，忽略大小写。
 */
class MissingLocaleInspection : LocalInspectionTool(), DumbAware {
    @JvmField var ignoredFileNames = "languages.yml"

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.expandableString("ignoredFileNames", ChronicleInspectionBundle.message("inspection.option.ignoredFileNames"), ",")
                .description(ChronicleBundle.message("comment.patterns"))
        )
    }

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 跳过需要忽略的文件
        if (file.name.matchesPatterns(ignoredFileNames, ignoreCase = true)) return false
        // 跳过内存文件和注入的文件
        val vFile = file.virtualFile
        if (VirtualFileService.isLightFile(vFile)) return false
        if (VirtualFileService.isInjectedFile(vFile)) return false
        // 要求是语义上有效的本地化文件
        return ParadoxPsiFileMatchService.isLocalisationFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiFileOnlyVisitor() {
            override fun visitFile(file: PsiFile) {
                ProgressManager.checkCanceled()
                check(file, holder)
            }
        }
    }

    private fun check(file: PsiFile, holder: ProblemsHolder) {
        if (file !is ParadoxLocalisationFile) return
        if (file.propertyLists.all { it.locale != null }) return // 没有问题，跳过
        val description = ChronicleInspectionBundle.message("inspection.localisation.missingLocale.desc")
        holder.registerProblem(file, description)
    }
}
