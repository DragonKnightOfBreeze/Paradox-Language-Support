package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.resolve.ParadoxParameterService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import javax.swing.JComponent

/**
 * 缺失的参数的代码检查。
 *
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class MissingParameterInspection : LocalInspectionTool() {
    @JvmField
    var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是符合条件的脚本文件
        val injectable = !ignoredInInjectedFiles
        return ParadoxPsiFileMatcher.isScriptFile(file, smart = true, injectable = injectable)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            private fun shouldVisit(element: PsiElement): Boolean {
                return (element is ParadoxScriptProperty && !element.name.isParameterized()) || element is ParadoxScriptString
            }

            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if (!shouldVisit(element)) return

                val from = ParadoxParameterContextReferenceInfo.From.ContextReference
                val contextConfig = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return
                val contextReferenceInfo = ParadoxParameterService.getContextReferenceInfo(element, from, contextConfig) ?: return
                if (contextReferenceInfo.contextName.isParameterized()) return // skip if context name is parameterized
                val argumentNames = contextReferenceInfo.arguments.mapTo(mutableSetOf()) { it.argumentName }
                val requiredParameterNames = mutableSetOf<String>()
                ParadoxParameterService.processContextReference(element, contextReferenceInfo, true) p@{
                    ProgressManager.checkCanceled()
                    val parameterContextInfo = ParadoxParameterService.getContextInfo(it) ?: return@p true
                    if (parameterContextInfo.parameters.isEmpty()) return@p true
                    parameterContextInfo.parameters.keys.forEach { parameterName ->
                        if (requiredParameterNames.contains(parameterName)) return@forEach
                        if (!ParadoxParameterManager.isOptional(parameterContextInfo, parameterName, argumentNames)) requiredParameterNames.add(parameterName)
                    }
                    false
                }
                requiredParameterNames.removeAll(argumentNames)
                if (requiredParameterNames.isEmpty()) return
                val rangeInElement = contextReferenceInfo.contextNameRange.shiftLeft(element.startOffset)
                if (rangeInElement.isEmpty || rangeInElement.startOffset < 0) return // 防止意外
                registerProblem(element, requiredParameterNames, rangeInElement)
            }

            private fun registerProblem(element: PsiElement, names: Set<String>, rangeInElement: TextRange? = null) {
                val description = when {
                    names.isEmpty() -> return
                    names.size == 1 -> PlsBundle.message("inspection.script.missingParameter.desc.1", names.single())
                    else -> PlsBundle.message("inspection.script.missingParameter.desc.2", names.joinToString(", "))
                }
                holder.registerProblem(element, rangeInElement, description)
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
        }
    }
}
