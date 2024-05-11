package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 无法解析的表达式的检查。
 *
 * @property ignoredByConfigs （配置项）如果对应的扩展的CWT规则存在，是否需要忽略此代码检查。
 */
class UnresolvedExpressionInspection : LocalInspectionTool() {
    @JvmField var showExpectInfo = true
    @JvmField var ignoredByConfigs = false
    
    //如果一个表达式（属性/值）无法解析，需要跳过直接检测下一个表达式，而不是向下检查它的子节点
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        var suppressed: PsiElement? = null
        
        val file = holder.file
        val project = holder.project
        val configGroup = getConfigGroup(project, selectGameType(file))
        
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                val result = when(element) {
                    is ParadoxScriptProperty -> visitProperty(element)
                    is ParadoxScriptValue -> visitValue(element)
                    else -> true
                }
                if(!result) suppressed = element
            }
            
            private fun visitProperty(element: ParadoxScriptProperty): Boolean {
                if(suppressed != null && suppressed.isAncestor(element)) return true
                
                //skip checking property if property key may contain parameters
                val propertyKey = element.propertyKey
                if(propertyKey.text.isParameterized()) return false
                val configContext = CwtConfigHandler.getConfigContext(element) ?: return true
                if(configContext.isDefinition()) return true
                if(configContext.getConfigs().isEmpty()) return true
                val configs = CwtConfigHandler.getConfigs(element)
                if(configs.isEmpty()) {
                    //优先使用重载后的规则
                    val expectedConfigs = getExpectedConfigs(element)
                    if(expectedConfigs.isNotEmpty()) {
                        //判断是否需要排除
                        if(isExcluded(configs)) return true
                        //判断是否需要忽略
                        if(isIgnoredByConfigs(element.propertyKey, configs)) return true
                    }
                    val expectedExpressions = expectedConfigs.mapTo(mutableSetOf()) { it.expression }
                    expectedExpressions.orNull()?.joinToString()
                    val expect = if(showExpectInfo) expectedExpressions.joinToString() else null
                    val message = when {
                        expect == null -> PlsBundle.message("inspection.script.unresolvedExpression.description.1.1", propertyKey.expression)
                        expect.isNotEmpty() -> PlsBundle.message("inspection.script.unresolvedExpression.description.1.2", propertyKey.expression, expect)
                        else -> PlsBundle.message("inspection.script.unresolvedExpression.description.1.3", propertyKey.expression)
                    }
                    val fixes = getFixes(element, expectedConfigs).toTypedArray()
                    holder.registerProblem(element, message, *fixes)
                    //skip checking children
                    return false
                }
                return true
            }
            
            private fun visitValue(element: ParadoxScriptValue): Boolean {
                if(!element.isExpression()) return false // skip check if element is not an expression
                
                if(suppressed != null && suppressed.isAncestor(element)) return true
                
                //also check if element is a scripted_variable_reference
                //skip checking value if it may contain parameters
                if(element is ParadoxScriptString && element.text.isParameterized()) return false
                if(element is ParadoxScriptScriptedVariableReference && element.text.isParameterized()) return false
                val configContext = CwtConfigHandler.getConfigContext(element) ?: return true
                if(configContext.isDefinition()) return true
                if(configContext.getConfigs().isEmpty()) return true
                val configs = CwtConfigHandler.getConfigs(element, orDefault = false)
                if(configs.isEmpty()) {
                    //优先使用重载后的规则
                    val expectedConfigs = getExpectedConfigs(element, configContext)
                    if(expectedConfigs.isNotEmpty()) {
                        //判断是否需要排除
                        if(isExcluded(configs)) return true
                        //判断是否需要忽略
                        if(isIgnoredByConfigs(element, configs)) return true
                    }
                    val expectedExpressions = expectedConfigs.mapTo(mutableSetOf()) { it.expression }
                    val expect = if(showExpectInfo) expectedExpressions.joinToString() else null
                    val message = when {
                        expect == null -> PlsBundle.message("inspection.script.unresolvedExpression.description.2.1", element.expression)
                        expect.isNotEmpty() -> PlsBundle.message("inspection.script.unresolvedExpression.description.2.2", element.expression, expect)
                        else -> PlsBundle.message("inspection.script.unresolvedExpression.description.2.3", element.expression)
                    }
                    val fixes = getFixes(element, expectedConfigs).toTypedArray()
                    holder.registerProblem(element, message, *fixes)
                    //skip checking children
                    return false
                }
                //any规则不需要再向下检查
                if(configs.any { it.expression.type == CwtDataTypes.Any }) {
                    return false
                }
                return true
            }
            
            private fun getExpectedConfigs(element: ParadoxScriptProperty): List<CwtPropertyConfig> {
                //这里使用合并后的子规则，即使parentProperty可以精确匹配
                val parentMemberElement = element.parentOfType<ParadoxScriptMemberElement>() ?: return emptyList()
                val parentConfigContext = CwtConfigHandler.getConfigContext(parentMemberElement) ?: return emptyList()
                return buildList {
                    val contextConfigs = parentConfigContext.getConfigs()
                    contextConfigs.forEachFast f@{ contextConfig ->
                        contextConfig.configs?.forEachFast f1@{ c1 ->
                            val c = if(c1 is CwtPropertyConfig) c1 else return@f1
                            val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(element, c)
                            if(overriddenConfigs.isNotNullOrEmpty()) {
                                addAll(overriddenConfigs)
                            } else {
                                add(c)
                            }
                        }
                    }
                }
            }
            
            private fun getExpectedConfigs(element: ParadoxScriptValue, configContext: CwtConfigContext): List<CwtValueConfig> {
                return buildList {
                    val contextConfigs = configContext.getConfigs()
                    contextConfigs.forEachFast f@{ contextConfig ->
                        val c = if(contextConfig is CwtValueConfig) contextConfig else return@f
                        val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(element, c)
                        if(overriddenConfigs.isNotNullOrEmpty()) {
                            addAll(overriddenConfigs)
                        } else {
                            add(c)
                        }
                    }
                }
            }
            
            private fun isExcluded(memberConfigs: List<CwtMemberConfig<*>>): Boolean {
                return memberConfigs.all { it.expression.type in CwtDataTypeGroups.PathReference }
            }
            
            private fun isIgnoredByConfigs(element: ParadoxScriptExpressionElement, memberConfigs: List<CwtMemberConfig<*>>): Boolean {
                if(!ignoredByConfigs) return false
                val value = element.value
                for(memberConfig in memberConfigs) {
                    val configExpression = memberConfig.expression
                    if(configExpression.type in CwtDataTypeGroups.DefinitionAware) {
                        val definitionType = configExpression.value ?: continue
                        val configs = configGroup.extendedDefinitions.findFromPattern(value, element, configGroup).orEmpty()
                        val config = configs.findFast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionType) }
                        if(config != null) return true
                        if(definitionType == "game_rule") {
                            val config1 = configGroup.extendedGameRules.findFromPattern(value, element, configGroup)
                            if(config1 != null) return true
                        }
                        if(definitionType == "on_action") {
                            val config1 = configGroup.extendedOnActions.findFromPattern(value, element, configGroup)
                            if(config1 != null) return true
                        }
                    }
                }
                return false
            }
            
            private fun getFixes(element: PsiElement, expectedConfigs: List<CwtMemberConfig<*>>): List<LocalQuickFix> {
                return buildList {
                    val expressionElement = when(element) {
                        is ParadoxScriptProperty -> element.propertyKey
                        is ParadoxScriptStringExpressionElement -> element
                        else -> null
                    }
                    if(expressionElement != null) {
                        val locales = ParadoxLocaleHandler.getLocaleConfigs()
                        val context = expectedConfigs.firstNotNullOfOrNull {
                            ParadoxLocalisationCodeInsightContext.fromReference(expressionElement, it, locales, fromInspection = true)
                        }
                        if(context != null) {
                            this += GenerateLocalisationsFix(expressionElement, context)
                            this += GenerateLocalisationsInFileFix(expressionElement)
                        }
                    }
                }
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            //showExpectInfo
            row {
                checkBox(PlsBundle.message("inspection.script.unresolvedExpression.option.showExpectInfo"))
                    .bindSelected(::showExpectInfo)
                    .actionListener { _, component -> showExpectInfo = component.isSelected }
            }
            //ignoredByConfigs
            row {
                checkBox(PlsBundle.message("inspection.script.unresolvedExpression.option.ignoredByConfigs"))
                    .bindSelected(::ignoredByConfigs)
                    .actionListener { _, component -> ignoredByConfigs = component.isSelected }
            }
        }
    }
}

