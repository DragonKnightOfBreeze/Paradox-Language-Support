package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.matchesPattern
import icu.windea.pls.core.splitOptimized
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.toCommaDelimitedString
import icu.windea.pls.core.toCommaDelimitedStringList
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import javax.swing.JComponent

/**
 * 检查本地化文件中是否包含多个语言环境声明。
 *
 * @property ignoredFileNames （配置项）需要忽略的文件名的模式。使用GLOB模式。忽略大小写。默认为"languages.yml"。
 */
class MultipleLocalesInspection : LocalInspectionTool(), DumbAware {
    @JvmField
    var ignoredFileNames = "languages.yml"

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 跳过内存文件
        if (PlsFileManager.isLightFile(file.virtualFile)) return false
        return true
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<out ProblemDescriptor>? {
        if (file !is ParadoxLocalisationFile) return null

        val fileName = file.name
        ignoredFileNames.splitOptimized(';').forEach {
            if (fileName.matchesPattern(it, true)) return null // 忽略
        }
        if (file.propertyLists.size <= 1) return null // 不存在多个语言环境，忽略
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("inspection.localisation.multipleLocales.desc")
        holder.registerProblem(file, description, ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
        return holder.resultsArray
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredFileNames
            row {
                label(PlsBundle.message("inspection.localisation.multipleLocales.option.ignoredFileNames"))
                    .applyToComponent { toolTipText = PlsBundle.message("inspection.localisation.multipleLocales.option.ignoredFileNames.tooltip") }
            }
            row {
                expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                    .bindText(::ignoredFileNames.toAtomicProperty())
                    .comment(PlsBundle.message("inspection.localisation.multipleLocales.option.ignoredFileNames.comment"))
                    .align(Align.FILL)
                    .resizableColumn()
            }
        }
    }
}
