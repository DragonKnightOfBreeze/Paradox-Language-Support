package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
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
                if(forInvocationExpressions && element is ParadoxScriptProperty && !element.name.isParameterizedExpression()) {
                    visitElementForInvocationExpression(element)
                } else if(forScriptValueExpressions && element is ParadoxScriptString) {
                    visitElementForScriptValueExpression(element)
                }
            }
            
            private fun visitElementForInvocationExpression(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                val configs = ParadoxConfigHandler.getConfigs(element)
                val config = configs.firstOrNull() as? CwtPropertyConfig ?: return
                //NOTE 不兼容本地化参数（CwtDataType.LocalisationParameter），因为那个引用也可能实际上对应一个缺失的本地化的名字
                val condition = config.configs?.any { it.expression.type == CwtDataType.Parameter } == true
                if(!condition) return
                
                val parameterNames = mutableSetOf<String>()
                val block = element.block ?: return
                block.processProperty(inline = true) {
                    parameterNames.add(it.name)
                    true
                }
                
                ProgressManager.checkCanceled()
                val requiredParameterNames = mutableSetOf<String>()
                ParadoxParameterSupportOld.processContextFromInvocationExpression(element, config) p@{
                    ProgressManager.checkCanceled()
                    val parameters = ParadoxParameterHandler.getParameters(it)
                    if(parameters.isNotEmpty()) {
                        parameters.forEach { (name, parameterInfo) ->
                            if(requiredParameterNames.contains(name)) return@forEach
                            if(!parameterInfo.optional) requiredParameterNames.add(name)
                        }
                    }
                    false
                }
                requiredParameterNames.removeAll(parameterNames)
                if(requiredParameterNames.isEmpty()) return
                val location = element.propertyKey
                registerProblem(location, requiredParameterNames)
            }
            
            private fun visitElementForScriptValueExpression(element: ParadoxScriptString) {
                ProgressManager.checkCanceled()
                val value = element.text
                if(value.isLeftQuoted()) return
                if(!value.startsWith("value:")) return //快速判断
                val config = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return
                val configGroup = config.info.configGroup
                val dataType = config.expression.type
                if(!dataType.isValueFieldType()) return
                val textRange = TextRange.create(0, value.length)
                val isKey = element is ParadoxScriptPropertyKey
                val valueFieldExpression = ParadoxValueFieldExpression.resolve(value, textRange, configGroup, isKey)
                if(valueFieldExpression == null) return
                val scriptValueExpression = valueFieldExpression.scriptValueExpression ?: return
                val scriptValueExpressionNode = scriptValueExpression.scriptValueNode
                
                val parameterNames = mutableSetOf<String>()
                scriptValueExpression.parameterNodes.forEach { parameterNames.add(it.text) }
                
                ProgressManager.checkCanceled()
                val requiredParameterNames = mutableSetOf<String>()
                val sv = scriptValueExpressionNode.getReference(element)?.resolve() ?: return //ignore
                if(sv !is ParadoxScriptProperty) return
                val parameters = ParadoxParameterHandler.getParameters(sv)
                if(parameters.isNotEmpty()) {
                    parameters.forEach { (name, parameterInfo) ->
                        if(requiredParameterNames.contains(name)) return@forEach
                        if(!parameterInfo.optional) requiredParameterNames.add(name)
                    }
                }
                requiredParameterNames.removeAll(parameterNames)
                if(requiredParameterNames.isEmpty()) return
                val location = element
                val range = scriptValueExpression.rangeInExpression
                registerProblem(location, requiredParameterNames, range)
            }
            
            private fun registerProblem(element: PsiElement, names: Set<String>, range: TextRange? = null) {
                val message = when {
                    names.isEmpty() -> return
                    names.size == 1 -> PlsBundle.message("inspection.script.general.missingParameter.description.1", names.single().let { "'$it'" })
                    else -> PlsBundle.message("inspection.script.general.missingParameter.description.2", names.joinToString(", ") { "'$it'" })
                }
                if(range == null) {
                    holder.registerProblem(element, message)
                } else {
                    holder.registerProblem(element, range, message)
                }
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