package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.matchesPattern
import icu.windea.pls.core.splitOptimized
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.util.psi.ParadoxPsiFileMatcher
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import javax.swing.JComponent

/**
 * 无法解析的图标的检查。
 *
 * @property ignoredNames （配置项）需要忽略的名字。使用GLOB模式。忽略大小写。
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedIconInspection : LocalInspectionTool() {
    @JvmField
    var ignoredNames = ""
    @JvmField
    var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        return ParadoxPsiFileMatcher.isLocalisationFile(file, smart = true, injectable = !ignoredInInjectedFiles)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationIcon) visitIcon(element)
            }

            private fun visitIcon(element: ParadoxLocalisationIcon) {
                val name = element.name ?: return
                ignoredNames.splitOptimized(';').forEach {
                    if (name.matchesPattern(it, true)) return // 忽略
                }
                val reference = element.reference
                if (reference == null || reference.resolve() != null) return
                val location = element.idElement ?: return
                holder.registerProblem(location, PlsBundle.message("inspection.localisation.unresolvedIcon.desc", name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                label(PlsBundle.message("inspection.localisation.unresolvedIcon.option.ignoredNames"))
            }
            row {
                textField()
                    .bindText(::ignoredNames.toAtomicProperty())
                    .comment(PlsBundle.message("inspection.localisation.unresolvedIcon.option.ignoredNames.comment"))
                    .align(Align.FILL)
                    .resizableColumn()
            }
            // ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
        }
    }
}
