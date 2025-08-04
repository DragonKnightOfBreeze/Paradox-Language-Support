package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.util.*
import javax.swing.*

/**
 * 无法解析的封装变量引用的检查。
 *
 * 提供快速修复：
 * * 声明本地封装变量（在同一文件中）
 * * 声明全局封装变量（在`common/scripted_variables`目录下的某一个文件中）
 * * 导入游戏目录或模组目录
 *
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles 是否在内联脚本文件中忽略此代码检查。
 */
class UnresolvedScriptedVariableInspection : LocalInspectionTool() {
    @JvmField
    var ignoredInInjectedFiles = false
    @JvmField
    var ignoredInInlineScriptFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (ignoredInInjectedFiles && PlsFileManager.isInjectedFile(file.virtualFile)) return false
        if (ignoredInInlineScriptFiles && ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return false
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if (element is ParadoxScriptedVariableReference) visitScriptedVariableReference(element)
            }

            private fun visitScriptedVariableReference(element: ParadoxScriptedVariableReference) {
                val name = element.name ?: return
                if (name.isParameterized()) return //skip if name is parameterized
                val reference = element.reference ?: return
                if (reference.resolve() != null) return
                val quickFixes = listOf<LocalQuickFix>(
                    IntroduceLocalVariableFix(name, element),
                    IntroduceGlobalVariableFix(name, element)
                )
                val message = PlsBundle.message("inspection.script.unresolvedScriptedVariable.desc", name)
                holder.registerProblem(element, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, *quickFixes.toTypedArray())
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            //ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles)
                    .actionListener { _, component -> ignoredInInjectedFiles = component.isSelected }
            }
            //ignoredInInlineScriptFiles
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInlineScriptFiles"))
                    .bindSelected(::ignoredInInlineScriptFiles)
                    .actionListener { _, component -> ignoredInInlineScriptFiles = component.isSelected }
            }
        }
    }
}

