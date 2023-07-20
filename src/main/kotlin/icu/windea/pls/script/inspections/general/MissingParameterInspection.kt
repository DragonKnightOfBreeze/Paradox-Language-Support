package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.psi.*

/**
 * 缺少的参数的检查。
 */
class MissingParameterInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : PsiElementVisitor() {
            private fun shouldVisit(element: PsiElement): Boolean {
                return (element is ParadoxScriptProperty && !element.name.isParameterized()) || element is ParadoxScriptString
            }
            
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(!shouldVisit(element)) return
                
                val from = ParadoxParameterContextReferenceInfo.From.ContextReference
                val contextConfig = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return
                val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(element, from, contextConfig) ?: return
                if(contextReferenceInfo.contextName.isParameterized()) return //skip if context name is parameterized
                val argumentNames = contextReferenceInfo.arguments.mapTo(mutableSetOf()) { it.argumentName }
                val requiredParameterNames = mutableSetOf<String>()
                ParadoxParameterSupport.processContext(element, contextReferenceInfo, true) p@{
                    ProgressManager.checkCanceled()
                    val parameterContextInfo = ParadoxParameterSupport.getContextInfo(it) ?: return@p true
                    if(parameterContextInfo.parameters.isEmpty()) return@p true
                    parameterContextInfo.parameters.keys.forEach { parameterName ->
                        if(requiredParameterNames.contains(parameterName)) return@forEach
                        if(!parameterContextInfo.isOptional(parameterName, argumentNames)) requiredParameterNames.add(parameterName)
                    }
                    false
                }
                requiredParameterNames.removeAll(argumentNames)
                if(requiredParameterNames.isEmpty()) return
                val rangeInElement = contextReferenceInfo.contextNameRange.shiftLeft(element.startOffset)
                if(rangeInElement.isEmpty || rangeInElement.startOffset < 0) return //防止意外
                registerProblem(element, requiredParameterNames, rangeInElement)
            }
            
            private fun registerProblem(element: PsiElement, names: Set<String>, rangeInElement: TextRange? = null) {
                val message = when {
                    names.isEmpty() -> return
                    names.size == 1 -> PlsBundle.message("inspection.script.general.missingParameter.description.1", names.single().let { "'$it'" })
                    else -> PlsBundle.message("inspection.script.general.missingParameter.description.2", names.joinToString(", ") { "'$it'" })
                }
                holder.registerProblem(element, rangeInElement, message)
            }
        }
    }
}