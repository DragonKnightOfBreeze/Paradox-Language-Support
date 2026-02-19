package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor
import javax.swing.JComponent

/**
 * 无法解析的颜色的代码检查。
 *
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedColorInspection : LocalInspectionTool() {
    @JvmField
    var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是可接受的本地化文件
        return ParadoxPsiFileMatcher.isLocalisationFile(file, injectable = !ignoredInInjectedFiles)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxLocalisationVisitor() {
            override fun visitColorfulText(element: ParadoxLocalisationColorfulText) {
                ProgressManager.checkCanceled()
                val name = element.name ?: return
                val reference = element.reference
                if (reference == null || reference.resolve() != null) return
                val location = element.idElement ?: return
                val description = PlsBundle.message("inspection.localisation.unresolvedColor.desc", name)
                holder.registerProblem(location, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
        }
    }
}
