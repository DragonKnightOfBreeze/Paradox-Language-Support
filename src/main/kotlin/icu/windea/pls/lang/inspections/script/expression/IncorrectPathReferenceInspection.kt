package icu.windea.pls.lang.inspections.script.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression
import javax.swing.JComponent

/**
 * （脚本文件中的）不正确的路径引用的代码检查。
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles （配置项）是否在内联脚本文件中忽略此代码检查。
 */
class IncorrectPathReferenceInspection : LocalInspectionTool() {
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var ignoredInInlineScriptFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 按需忽略注入的文件
        val vFile = file.virtualFile
        if (ignoredInInjectedFiles && VirtualFileService.isInjectedFile(vFile)) return false
        // 按需忽略内联脚本文件
        if (ignoredInInlineScriptFiles && ParadoxInlineScriptManager.isInlineScriptFile(file)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptStringExpressionElement) visitExpressionElement(element)
            }

            private fun visitExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                if (!element.isDataExpression()) return // skip check if element is not an expression

                // 忽略可能包含参数的表达式
                if (element.text.isParameterized()) return

                // 得到完全匹配的规则
                val config = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(fallback = false)).firstOrNull() ?: return

                val configExpression = config.configExpression
                val dataType = configExpression.type
                if (dataType !in CwtDataTypeSets.PathReference) return
                if (dataType == CwtDataTypes.Icon) return // no file extension in expression
                val expectFileExtensions = config.optionData.fileExtensions.orEmpty()
                if (expectFileExtensions.isEmpty()) return
                val value = element.value
                val fileExtension = value.substringAfterLast('.', "")
                if (expectFileExtensions.any { fileExtension.equals(it, true) }) return
                val description = ChronicleBundle.message("inspection.script.incorrectPathReference.desc.1", value, expectFileExtensions.joinToString())
                holder.registerProblem(element, description)
            }
        }
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
