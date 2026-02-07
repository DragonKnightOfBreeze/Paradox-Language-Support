package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptString
import javax.swing.JComponent

/**
 * 不正确的路径引用的代码检查。
 *
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles 是否在内联脚本文件中忽略此代码检查。
 */
class IncorrectPathReferenceInspection : LocalInspectionTool() {
    @JvmField
    var ignoredInInjectedFiles = false
    @JvmField
    var ignoredInInlineScriptFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 判断是否需要忽略内联脚本文件
        if (ignoredInInlineScriptFiles && ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return false
        // 要求是可接受的脚本文件
        return ParadoxPsiFileMatcher.isScriptFile(file, injectable = !ignoredInInjectedFiles)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptString) visitExpressionElement(element)
            }

            private fun visitExpressionElement(element: ParadoxScriptString) {
                ProgressManager.checkCanceled()

                // 忽略可能包含参数的表达式
                if (element.text.isParameterized()) return
                // 得到完全匹配的规则
                val config = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(fallback = false)).firstOrNull() ?: return
                val configExpression = config.configExpression
                val dataType = configExpression.type
                if (dataType == CwtDataTypes.AbsoluteFilePath) return
                if (dataType !in CwtDataTypeSets.PathReference) return
                val fileExtensions = config.optionData.fileExtensions.orEmpty()
                if (fileExtensions.isEmpty()) return
                val value = element.value
                if (fileExtensions.any { value.endsWith(it, true) }) return
                val extensionsString = fileExtensions.joinToString()
                val description = PlsBundle.message("inspection.script.incorrectPathReference.desc.1", value, extensionsString)
                holder.registerProblem(element, description)
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
            // ignoredInInlineScriptFiles
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInlineScriptFiles"))
                    .bindSelected(::ignoredInInlineScriptFiles.toAtomicProperty())
            }
        }
    }
}
