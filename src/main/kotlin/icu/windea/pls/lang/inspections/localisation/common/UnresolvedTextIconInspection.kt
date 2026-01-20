package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.matchesPattern
import icu.windea.pls.core.splitOptimized
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import javax.swing.JComponent

/**
 * 无法解析的文本图标的代码检查。
 *
 * @property ignoredNames （配置项）需要忽略的名字。使用GLOB模式。忽略大小写。
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
@WithGameType(ParadoxGameType.Ck3, ParadoxGameType.Vic3, ParadoxGameType.Eu5)
class UnresolvedTextIconInspection : LocalInspectionTool() {
    @JvmField
    var ignoredNames = ""
    @JvmField
    var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求游戏类型支持文本图标
        if (!ParadoxSyntaxConstraint.LocalisationTextIcon.test(file)) return false
        // 要求是符合条件的本地化文件
        val injectable = !ignoredInInjectedFiles
        return ParadoxPsiFileMatcher.isLocalisationFile(file, smart = true, injectable = injectable)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxLocalisationVisitor() {
            override fun visitTextIcon(element: ParadoxLocalisationTextIcon) {
                ProgressManager.checkCanceled()
                val name = element.name ?: return
                ignoredNames.splitOptimized(';').forEach {
                    if (name.matchesPattern(it, true)) return // 忽略
                }
                val reference = element.reference
                if (reference == null || reference.resolve() != null) return
                val location = element.idElement ?: return
                val description = PlsBundle.message("inspection.localisation.unresolvedTextIcon.desc", name)
                holder.registerProblem(location,  description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredNames
            row {
                label(PlsBundle.message("inspection.localisation.unresolvedTextIcon.option.ignoredNames"))
            }
            row {
                textField()
                    .bindText(::ignoredNames.toAtomicProperty())
                    .comment(PlsBundle.message("inspection.localisation.unresolvedTextIcon.option.ignoredNames.comment"))
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
