package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.parameter.ParadoxParameterSupport
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString

/**
 * 缺少的参数的检查。
 */
class MissingParameterInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            private fun shouldVisit(element: PsiElement): Boolean {
                return (element is ParadoxScriptProperty && !element.name.isParameterized()) || element is ParadoxScriptString
            }

            override fun visitElement(element: PsiElement) {
                if (!shouldVisit(element)) return

                val from = ParadoxParameterContextReferenceInfo.From.ContextReference
                val contextConfig = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return
                val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(element, from, contextConfig) ?: return
                if (contextReferenceInfo.contextName.isParameterized()) return //skip if context name is parameterized
                val argumentNames = contextReferenceInfo.arguments.mapTo(mutableSetOf()) { it.argumentName }
                val requiredParameterNames = mutableSetOf<String>()
                ParadoxParameterSupport.processContext(element, contextReferenceInfo, true) p@{
                    ProgressManager.checkCanceled()
                    val parameterContextInfo = ParadoxParameterSupport.getContextInfo(it) ?: return@p true
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
                if (rangeInElement.isEmpty || rangeInElement.startOffset < 0) return //防止意外
                registerProblem(element, requiredParameterNames, rangeInElement)
            }

            private fun registerProblem(element: PsiElement, names: Set<String>, rangeInElement: TextRange? = null) {
                val message = when {
                    names.isEmpty() -> return
                    names.size == 1 -> PlsBundle.message("inspection.script.missingParameter.desc.1", names.single().let { "'$it'" })
                    else -> PlsBundle.message("inspection.script.missingParameter.desc.2", names.joinToString(", ") { "'$it'" })
                }
                holder.registerProblem(element, rangeInElement, message)
            }
        }
    }
}
