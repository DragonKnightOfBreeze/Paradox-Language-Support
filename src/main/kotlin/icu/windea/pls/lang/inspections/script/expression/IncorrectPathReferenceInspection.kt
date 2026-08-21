package icu.windea.pls.lang.inspections.script.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression

/**
 * （脚本文件中的）不正确的路径引用的代码检查。
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles （配置项）是否在内联脚本文件中忽略此代码检查。
 */
class IncorrectPathReferenceInspection : LocalInspectionTool() {
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var ignoredInInlineScriptFiles = false
    @JvmField var showExpect = true

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("ignoredInInjectedFiles", ChronicleBundle.message("inspection.option.ignoredInInjectedFiles")),
            OptPane.checkbox("ignoredInInlineScriptFiles", ChronicleBundle.message("inspection.option.ignoredInInlineScriptFiles")),
            OptPane.checkbox("showExpect", ChronicleBundle.message("inspection.option.showExpect")),
        )
    }

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
        return object : ParadoxPsiElementVisitor() {
            override fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    private fun check(element: ParadoxScriptStringExpressionElement, holder: ProblemsHolder) {
        if (!element.isDataExpression()) return // skip if is not a data expression
        if (element.text.isParameterized()) return // skip if is parameterized

        // 得到完全匹配的规则
        val config = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(fallback = false)).firstOrNull() ?: return
        val configExpression = config.configExpression
        val dataType = configExpression.type
        if (dataType !in CwtDataTypeSets.PathReference) return
        if (dataType == CwtDataTypes.Icon) return // no file extension in expression
        val expectedFileExtensions = config.optionMetadata.fileExtensions.orEmpty()
        if (expectedFileExtensions.isEmpty()) return
        val value = element.value
        val fileExtension = value.substringAfterLast('.', "")
        if (expectedFileExtensions.any { fileExtension.equals(it, true) }) return
        reportProblem(element, value, expectedFileExtensions, holder)
    }

    private fun reportProblem(location: PsiElement, value: String, expectFileExtensions: Set<String>, holder: ProblemsHolder) {
        val expectText = expectFileExtensions.joinToString()
        val description = when {
            showExpect -> ChronicleBundle.message("inspection.script.incorrectPathReference.desc.1", value, expectText)
            else -> ChronicleBundle.message("inspection.script.incorrectPathReference.desc.0", value)
        }
        holder.registerProblem(location, description)
    }
}
