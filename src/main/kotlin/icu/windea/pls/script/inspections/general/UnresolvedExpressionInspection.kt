package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 无法解析的表达式的检查。
 */
class UnresolvedExpressionInspection : LocalInspectionTool() {
    @JvmField var showExpectInfo = true
    @JvmField var checkPropertyKey = true
    @JvmField var checkPropertyValue = true
    @JvmField var checkValue = true
    
    //从定义级别开始向下检查
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptDefinitionElement) {
                    val definitionInfo = element.definitionInfo
                    if(definitionInfo != null) {
                        visitDefinition(element, definitionInfo)
                    }
                }
            }
            
            @Suppress("UNUSED_PARAMETER")
            private fun visitDefinition(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
                element.accept(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        val result = when(element) {
                            is ParadoxScriptProperty -> visitProperty(element)
                            is ParadoxScriptValue -> visitValue(element)
                            else -> true
                        }
                        if(result && element.isExpressionOrMemberContext()) super.visitElement(element)
                    }
                    
                    private fun visitProperty(element: ParadoxScriptProperty): Boolean {
                        val shouldCheck = checkPropertyKey
                        if(!shouldCheck) return true
                        //skip checking property if property key may contain parameters
                        val propertyKey = element.propertyKey
                        if(propertyKey.text.isParameterized()) return false
                        val configContext = ParadoxConfigHandler.getConfigContext(element) ?: return true
                        if(configContext.getConfigs().isEmpty()) return true
                        val configs = ParadoxConfigHandler.getConfigs(element)
                        if(configs.isEmpty()) {
                            //这里使用合并后的子规则，即使parentProperty可以精确匹配
                            //优先使用重载后的规则
                            val expect = if(showExpectInfo) {
                                val allConfigs = getAllConfigs(element)
                                //某些情况下我们需要忽略一些未解析的表达式
                                if(allConfigs.isNotEmpty() && allConfigs.all { isIgnored(it) }) return true
                                val allExpressions = allConfigs.mapTo(mutableSetOf()) { it.expression }
                                allExpressions.takeIfNotEmpty()?.joinToString()
                            } else null
                            val message = when {
                                expect == null -> PlsBundle.message("inspection.script.general.unresolvedExpression.description.1.1", propertyKey.expression)
                                expect.isNotEmpty() -> PlsBundle.message("inspection.script.general.unresolvedExpression.description.1.2", propertyKey.expression, expect)
                                else -> PlsBundle.message("inspection.script.general.unresolvedExpression.description.1.3", propertyKey.expression)
                            }
                            holder.registerProblem(element, message)
                            return false
                        }
                        return true
                    }
                    
                    private fun visitValue(element: ParadoxScriptValue): Boolean {
                        ProgressManager.checkCanceled()
                        val shouldCheck = when {
                            //also check if element is a scripted_variable
                            //element is ParadoxScriptedVariableReference -> return false
                            element.isPropertyValue() -> checkPropertyValue
                            element.isBlockValue() -> checkValue
                            else -> return false //skip
                        }
                        if(!shouldCheck) return true
                        //skip checking value if it may contain parameters
                        if(element is ParadoxScriptString && element.text.isParameterized()) return false
                        if(element is ParadoxScriptScriptedVariableReference && element.text.isParameterized()) return false
                        val configContext = ParadoxConfigHandler.getConfigContext(element) ?: return true
                        if(configContext.getConfigs().isEmpty()) return true
                        val configs = ParadoxConfigHandler.getConfigs(element, orDefault = false)
                        if(configs.isEmpty()) {
                            val expect = if(showExpectInfo) {
                                //优先使用重载后的规则
                                val allConfigs = getAllConfigs(element, configContext)
                                //某些情况下我们需要忽略一些未解析的表达式
                                if(allConfigs.isNotEmpty() && allConfigs.all { isIgnored(it) }) return true
                                val allExpressions = allConfigs.mapTo(mutableSetOf()) { it.expression }
                                allExpressions.takeIfNotEmpty()?.joinToString()
                            } else null
                            val message = when {
                                expect == null -> PlsBundle.message("inspection.script.general.unresolvedExpression.description.2.1", element.expression)
                                expect.isNotEmpty() -> PlsBundle.message("inspection.script.general.unresolvedExpression.description.2.2", element.expression, expect)
                                else -> PlsBundle.message("inspection.script.general.unresolvedExpression.description.2.3", element.expression)
                            }
                            holder.registerProblem(element, message)
                            //skip checking children
                            return false
                        }
                        //any规则不需要再向下检查
                        if(configs.any { it.expression.type == CwtDataType.Any }) {
                            return false
                        }
                        return true
                    }
                    
                    private fun getAllConfigs(element: ParadoxScriptProperty): List<CwtPropertyConfig> {
                        val parentMemberElement = element.parentOfType<ParadoxScriptMemberElement>() ?: return emptyList()
                        val parentConfigContext = ParadoxConfigHandler.getConfigContext(parentMemberElement) ?: return emptyList()
                        return buildList {
                            val contextConfigs = parentConfigContext.getConfigs()
                            contextConfigs.forEachFast f@{
                                val c = if(it is CwtPropertyConfig) it else return@f
                                val overriddenConfigs = ParadoxOverriddenConfigProvider.getOverriddenConfigs(element, c)
                                if(overriddenConfigs.isNotNullOrEmpty()) {
                                    addAll(overriddenConfigs)
                                } else {
                                    add(c)
                                }
                            }
                        }
                    }
                    
                    private fun getAllConfigs(element: ParadoxScriptValue, configContext: ParadoxConfigContext): List<CwtValueConfig> {
                        return buildList {
                            val contextConfigs = configContext.getConfigs()
                            contextConfigs.forEachFast f@{
                                val c = if(it is CwtValueConfig) it else return@f
                                val overriddenConfigs = ParadoxOverriddenConfigProvider.getOverriddenConfigs(element, c)
                                if(overriddenConfigs.isNotNullOrEmpty()) {
                                    addAll(overriddenConfigs)
                                } else {
                                    add(c)
                                }
                            }
                        }
                    }
                    
                    private fun isIgnored(config: CwtMemberConfig<*>): Boolean {
                        return config.expression.type.isPathReferenceType()
                    }
                })
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                checkBox(PlsBundle.message("inspection.script.general.unresolvedExpression.option.showExpectInfo"))
                    .bindSelected(::showExpectInfo)
                    .actionListener { _, component -> showExpectInfo = component.isSelected }
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.unresolvedExpression.option.checkPropertyKey"))
                    .bindSelected(::checkPropertyKey)
                    .actionListener { _, component -> checkPropertyKey = component.isSelected }
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.unresolvedExpression.option.checkPropertyValue"))
                    .bindSelected(::checkPropertyValue)
                    .actionListener { _, component -> checkPropertyValue = component.isSelected }
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.unresolvedExpression.option.checkValue"))
                    .bindSelected(::checkValue)
                    .actionListener { _, component -> checkValue = component.isSelected }
            }
        }
    }
}

