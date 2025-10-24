package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.quickfix.IntroduceGlobalVariableFix
import icu.windea.pls.lang.quickfix.IntroduceLocalVariableFix
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.localisation.psi.ParadoxLocalisationScriptedVariableReference
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
        return ParadoxPsiFileMatcher.isLocalisationFile(file, smart = true, injectable = !ignoredInInjectedFiles)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationScriptedVariableReference) visitScriptedVariableReference(element)
            }

            private fun visitScriptedVariableReference(element: ParadoxLocalisationScriptedVariableReference) {
                val name = element.name ?: return
                if (name.isParameterized()) return // skip if name is parameterized
                val reference = element.reference ?: return
                if (reference.resolve() != null) return
                val message = PlsBundle.message("inspection.localisation.unresolvedScriptedVariable.desc", name)
                val quickFixes = getFixes(element, name)
                holder.registerProblem(element, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, *quickFixes)
            }
        }
    }

    private fun getFixes(element: ParadoxLocalisationScriptedVariableReference, name: String): Array<LocalQuickFix> {
        return arrayOf(
            IntroduceLocalVariableFix(name, element),
            IntroduceGlobalVariableFix(name, element)
        )
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles)
                    .actionListener { _, component -> ignoredInInjectedFiles = component.isSelected }
            }
        }
    }
}
