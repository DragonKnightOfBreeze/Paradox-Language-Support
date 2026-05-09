package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.matchesPatterns
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.toCommaDelimitedString
import icu.windea.pls.core.toCommaDelimitedStringList
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import javax.swing.JComponent

/**
 * 检查本地化文件中是否缺少语言环境声明。
 *
 * @property ignoredFileNames （配置项）需要忽略的文件名的模式。使用GLOB模式。忽略大小写。
 */
class MissingLocaleInspection : LocalInspectionTool(), DumbAware {
    @JvmField
    var ignoredFileNames = ""

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 跳过内存文件
        if (VirtualFileService.isLightFile(file.virtualFile)) return false
        // 要求是可接受的本地化文件
        return ParadoxPsiFileMatcher.isLocalisationFile(file, injectable = true)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<out ProblemDescriptor>? {
        if (file !is ParadoxLocalisationFile) return null

        val fileName = file.name
        if (fileName.matchesPatterns(ignoredFileNames, ignoreCase = true)) return null // 忽略

        if (file.propertyLists.all { it.locale != null }) return null // 没有问题，跳过
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("inspection.localisation.missingLocale.desc")
        holder.registerProblem(file, description)
        return holder.resultsArray
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredFileNames
            row {
                label(PlsBundle.message("inspection.localisation.missingLocale.option.ignoredFileNames"))
            }
            row {
                expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                    .bindText(::ignoredFileNames.toAtomicProperty())
                    .comment(PlsBundle.message("inspection.localisation.missingLocale.option.ignoredFileNames.comment"))
                    .align(Align.FILL)
                    .resizableColumn()
            }
        }
    }
}
