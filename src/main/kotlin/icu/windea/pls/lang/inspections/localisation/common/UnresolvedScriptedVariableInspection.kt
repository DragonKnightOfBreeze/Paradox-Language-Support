package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.quickfix.IntroduceGlobalVariableFix
import icu.windea.pls.lang.quickfix.IntroduceLocalScriptedVariableFix
import icu.windea.pls.localisation.psi.ParadoxLocalisationScriptedVariableReference
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor
import javax.swing.JComponent

/**
 * 无法解析的封装变量引用的代码检查。
 *
 * 提供快速修复：
 * - 声明全局封装变量（在`common/scripted_variables`目录下的某一文件中）
 * - 导入游戏目录或模组目录
 *
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedScriptedVariableInspection : LocalInspectionTool() {
    @JvmField
    var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求是符合条件的本地化文件
        val injectable = !ignoredInInjectedFiles
        return ParadoxPsiFileMatcher.isLocalisationFile(file, smart = true, injectable = injectable)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxLocalisationVisitor() {
            override fun visitScriptedVariableReference(element: ParadoxLocalisationScriptedVariableReference) {
                ProgressManager.checkCanceled()
                val name = element.name ?: return
                if (name.isParameterized()) return // skip if name is parameterized
                val reference = element.reference ?: return
                if (reference.resolve() != null) return
                val description = PlsBundle.message("inspection.localisation.unresolvedScriptedVariable.desc", name)
                val fixes = getFixes(element, name)
                holder.registerProblem(element, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, *fixes)
            }
        }
    }

    private fun getFixes(element: ParadoxLocalisationScriptedVariableReference, name: String): Array<LocalQuickFix> {
        return arrayOf(
            IntroduceLocalScriptedVariableFix(name, element),
            IntroduceGlobalVariableFix(name, element),
        )
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
