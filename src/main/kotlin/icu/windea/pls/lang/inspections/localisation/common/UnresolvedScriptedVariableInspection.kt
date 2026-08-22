package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.fixes.IntroduceGlobalVariableFix
import icu.windea.pls.lang.fixes.IntroduceLocalScriptedVariableFix
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference

/**
 * 检查是否存在无法解析的封装变量引用。
 *
 * 提供快速修复：
 * - 声明全局封装变量（在 `common/scripted_variables` 目录下的某一文件中）
 * - 导入游戏目录或模组目录
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedScriptedVariableInspection : LocalInspectionTool() {
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
        // 要求是语义上有效的本地化文件
        return ParadoxPsiFileMatchService.isLocalisationFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxPsiElementVisitor() {
            override fun visitScriptedVariableReference(element: ParadoxScriptedVariableReference) {
                super.visitScriptedVariableReference(element)
                check(element, holder)
            }
        }
    }

    private fun check(element: ParadoxScriptedVariableReference, holder: ProblemsHolder) {
        val name = element.name ?: return
        if (name.isParameterized()) return // skip if name is parameterized
        val reference = element.reference ?: return
        if (reference.resolve() != null) return
        val description = ChronicleInspectionBundle.message("localisation.unresolvedScriptedVariable.desc", name)
        val fixes = getFixes(element, name)
        holder.registerProblem(element, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, *fixes)
    }

    private fun getFixes(element: ParadoxScriptedVariableReference, name: String): Array<LocalQuickFix> {
        return arrayOf(
            IntroduceLocalScriptedVariableFix(name, element),
            IntroduceGlobalVariableFix(name, element),
        )
    }
}
