package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 缺少的参数的检查。
 *
 * @property forInvocationExpressions 是否对调用表达式进行检查。（`some_effect = {PARAM = some_value}`）
 * @property forScriptValueExpressions 是否对SV表达式进行检查。（`some_prop = value:some_sv|PARAM|value|`）
 */
class MissingParameterInspection : LocalInspectionTool() {
    @JvmField var forInvocationExpressions = true
    @JvmField var forScriptValueExpressions = true
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(forInvocationExpressions && element is ParadoxScriptProperty && !element.name.isParameterized()) {
                    visitElementFromContextReferenceElement(element)
                } else if(forScriptValueExpressions && element is ParadoxScriptString) {
                    visitElementFromContextReferenceElement(element)
                }
            }
            
            private fun visitElementFromContextReferenceElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                val from = ParadoxParameterContextReferenceInfo.From.ContextReference
                val contextConfig = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return
                val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(element, from, contextConfig) ?: return
                if(contextReferenceInfo.contextName.isParameterized()) return //skip if context name is parameterized
                val argumentNames = contextReferenceInfo.argumentNames
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
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                checkBox(PlsBundle.message("inspection.script.general.missingParameter.option.forInvocationExpressions"))
                    .bindSelected(::forInvocationExpressions)
                    .applyToComponent { toolTipText = PlsBundle.message("inspection.script.general.missingParameter.option.forInvocationExpressions.tooltip") }
                    .actionListener { _, component -> forInvocationExpressions = component.isSelected }
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.missingParameter.option.forScriptValueExpressions"))
                    .bindSelected(::forScriptValueExpressions)
                    .applyToComponent { toolTipText = PlsBundle.message("inspection.script.general.missingParameter.option.forScriptValueExpressions.tooltip") }
                    .actionListener { _, component -> forScriptValueExpressions = component.isSelected }
            }
        }
    }
}