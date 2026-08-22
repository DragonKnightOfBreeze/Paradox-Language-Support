package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 缺失的参数的代码检查。
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class MissingParameterInspection : LocalInspectionTool() {
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
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                if (element.name.isParameterized()) return // skip if property key is parameterized
                check(element, holder)
            }

            override fun visitValue(element: ParadoxScriptValue) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    private fun check(element: ParadoxScriptMember, holder: ProblemsHolder) {
        val from = ParadoxParameterContextReferenceInfo.From.ContextReference
        val contextConfig = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return
        val contextReferenceInfo = ParadoxParameterManager.getContextReferenceInfo(element, from, contextConfig) ?: return
        if (contextReferenceInfo.contextName.isParameterized()) return // skip if context name is parameterized
        val requiredParameterNames = ParadoxParameterManager.getRequiredParameterNames(element, contextReferenceInfo)
        if (requiredParameterNames.isEmpty()) return
        val rangeInElement = contextReferenceInfo.contextNameRange.shiftLeft(element.startOffset)
        if (rangeInElement.isEmpty || rangeInElement.startOffset < 0) return // 防止意外
        registerProblem(element, requiredParameterNames, rangeInElement, holder)
    }

    private fun registerProblem(element: PsiElement, names: Set<String>, rangeInElement: TextRange? = null, holder: ProblemsHolder) {
        val description = when {
            names.isEmpty() -> return
            names.size == 1 -> ChronicleInspectionBundle.message("script.missingParameter.desc.1", names.single())
            else -> ChronicleInspectionBundle.message("script.missingParameter.desc.2", names.joinToString(", "))
        }
        holder.registerProblem(element, rangeInElement, description)
    }
}
