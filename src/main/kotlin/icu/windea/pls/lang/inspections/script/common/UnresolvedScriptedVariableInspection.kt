package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.fixes.IntroduceGlobalVariableFix
import icu.windea.pls.lang.fixes.IntroduceLocalScriptedVariableFix
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import javax.swing.JComponent

/**
 * 无法解析的封装变量引用的代码检查。
 *
 * 提供快速修复：
 * - 声明本地封装变量（在同一文件中）
 * - 声明全局封装变量（在 `common/scripted_variables` 目录下的某一个文件中）
 * - 导入游戏目录或模组目录
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles （配置项）是否在内联脚本文件中忽略此代码检查。
 */
class UnresolvedScriptedVariableInspection : LocalInspectionTool() {
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var ignoredInInlineScriptFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 按需忽略注入的文件
        val vFile = file.virtualFile
        if (ignoredInInjectedFiles && VirtualFileService.isInjectedFile(vFile)) return false
        // 按需忽略内联脚本文件
        if (ignoredInInlineScriptFiles && ParadoxInlineScriptManager.isInlineScriptFile(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptedVariableReference) visitScriptedVariableReference(element)
            }

            private fun visitScriptedVariableReference(element: ParadoxScriptedVariableReference) {
                ProgressManager.checkCanceled()
                val name = element.name ?: return
                if (name.isParameterized()) return // skip if name is parameterized
                val reference = element.reference ?: return
                if (reference.resolve() != null) return
                val description = ChronicleBundle.message("inspection.script.unresolvedScriptedVariable.desc", name)
                val fixes = getFixes(element, name)
                holder.registerProblem(element, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, *fixes)
            }
        }
    }

    private fun getFixes(element: ParadoxScriptedVariableReference, name: String): Array<LocalQuickFix> {
        return arrayOf(
            IntroduceLocalScriptedVariableFix(name, element),
            IntroduceGlobalVariableFix(name, element),
        )
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredInInjectedFile
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
            // ignoredInInlineScriptFiles
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredInInlineScriptFiles"))
                    .bindSelected(::ignoredInInlineScriptFiles.toAtomicProperty())
            }
        }
    }
}

