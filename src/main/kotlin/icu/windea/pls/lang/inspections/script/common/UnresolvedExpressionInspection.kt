package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 无法解析的表达式的检查。
 *
 * @property ignoredByConfigs （配置项）如果对应的扩展的CWT规则存在，是否需要忽略此代码检查。
 */
class UnresolvedExpressionInspection : LocalInspectionTool() {
    @JvmField
    var showExpectInfo = true
    @JvmField
    var ignoredByConfigs = false

    //如果一个表达式（属性/值）无法解析，需要跳过直接检测下一个表达式，而不是继续向下检查它的子节点

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        var suppressed: PsiElement? = null
        val file = holder.file
        val project = holder.project
        val configGroup = PlsFacade.getConfigGroup(project, selectGameType(file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                val result = when (element) {
                    is ParadoxScriptProperty -> visitProperty(element)
                    is ParadoxScriptValue -> visitValue(element)
                    else -> true
                }
                if (!result) suppressed = element
            }

            private fun visitProperty(element: ParadoxScriptProperty): Boolean {
                if (suppressed != null && suppressed.isAncestor(element)) return true

                //skip checking property if property key is parameterized
                val propertyKey = element.propertyKey
                if (propertyKey.text.isParameterized()) return false

                //NOTE if code is skipped by following checks, it may still be unresolved in fact, should be optimized in the future

                //skip if config context not exists
                val configContext = ParadoxExpressionManager.getConfigContext(element) ?: return true
                //skip if config context is not suitable
                if (!configContext.isRootOrMember() || configContext.isDefinition()) return true
                //skip if there are no context configs
                if (configContext.getConfigs().isEmpty()) return true

                val configs = ParadoxExpressionManager.getConfigs(element)
                if (configs.isEmpty()) {
                    val expectedConfigs = getExpectedConfigs(element)
                    if (expectedConfigs.isNotEmpty()) {
                        //判断是否需要排除
                        if (isExcluded(expectedConfigs)) return true
                        //判断是否需要忽略
                        if (isIgnoredByConfigs(element.propertyKey, expectedConfigs)) return true
                    }
                    val expectedExpressions = expectedConfigs.mapTo(mutableSetOf()) { it.configExpression.expressionString }
                    val expect = if (showExpectInfo) expectedExpressions.truncate(PlsConstants.Settings.itemLimit).joinToString() else null
                    val message = when {
                        expect == null -> PlsBundle.message("inspection.script.unresolvedExpression.desc.1.1", propertyKey.expression)
                        expect.isNotEmpty() -> PlsBundle.message("inspection.script.unresolvedExpression.desc.1.2", propertyKey.expression, expect)
                        else -> PlsBundle.message("inspection.script.unresolvedExpression.desc.1.3", propertyKey.expression)
                    }
                    val fixes = getFixes(element, expectedConfigs).toTypedArray()
                    holder.registerProblem(element, message, *fixes)
                    //skip checking children
                    return false
                }
                return continueCheck(configs)
            }

            private fun visitValue(element: ParadoxScriptValue): Boolean {
                if (!element.isExpression()) return false // skip check if element is not an expression

                if (suppressed != null && suppressed.isAncestor(element)) return true

                //skip checking value if it is parameterized
                if (element is ParadoxScriptString && element.text.isParameterized()) return false
                if (element is ParadoxScriptScriptedVariableReference && element.text.isParameterized()) return false

                //NOTE if code is skipped by following checks, it may still be unresolved in fact, should be optimized in the future

                //skip if config context not exists
                val configContext = ParadoxExpressionManager.getConfigContext(element) ?: return true
                //skip if config context is not suitable
                if (!configContext.isRootOrMember()) return true
                //skip if there are no context configs
                if (configContext.getConfigs().isEmpty()) return true

                val configs = ParadoxExpressionManager.getConfigs(element, orDefault = false)
                if (configs.isEmpty()) {
                    //skip check value if it is a special tag and there are no matched configs
                    if (element.tagType() != null) return false

                    val expectedConfigs = getExpectedConfigs(element, configContext)
                    if (expectedConfigs.isNotEmpty()) {
                        //判断是否需要排除
                        if (isExcluded(expectedConfigs)) return true
                        //判断是否需要忽略
                        if (isIgnoredByConfigs(element, expectedConfigs)) return true
                    }
                    val expectedExpressions = expectedConfigs.mapTo(mutableSetOf()) { it.configExpression.expressionString }
                    val expect = if (showExpectInfo) expectedExpressions.truncate(PlsConstants.Settings.itemLimit).joinToString() else null
                    val message = when {
                        expect == null -> PlsBundle.message("inspection.script.unresolvedExpression.desc.2.1", element.expression)
                        expect.isNotEmpty() -> PlsBundle.message("inspection.script.unresolvedExpression.desc.2.2", element.expression, expect)
                        else -> PlsBundle.message("inspection.script.unresolvedExpression.desc.2.3", element.expression)
                    }
                    val fixes = getFixes(element, expectedConfigs).toTypedArray()
                    holder.registerProblem(element, message, *fixes)
                    //skip checking children
                    return false
                }
                return continueCheck(configs)
            }

            private fun getExpectedConfigs(element: ParadoxScriptProperty): List<CwtPropertyConfig> {
                //这里使用合并后的子规则，即使parentProperty可以精确匹配
                val parentMemberElement = element.parentOfType<ParadoxScriptMemberElement>() ?: return emptyList()
                val parentConfigContext = ParadoxExpressionManager.getConfigContext(parentMemberElement) ?: return emptyList()
                return buildList {
                    val contextConfigs = parentConfigContext.getConfigs()
                    contextConfigs.forEach f@{ contextConfig ->
                        contextConfig.configs?.forEach f1@{ c1 ->
                            val c = if (c1 is CwtPropertyConfig) c1 else return@f1
                            //优先使用重载后的规则
                            val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(element, c)
                            if (overriddenConfigs.isNotNullOrEmpty()) {
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
                    contextConfigs.forEach f@{ contextConfig ->
                        val c = if (contextConfig is CwtValueConfig) contextConfig else return@f
                        val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(element, c)
                        if (overriddenConfigs.isNotNullOrEmpty()) {
                            addAll(overriddenConfigs)
                        } else {
                            add(c)
                        }
                    }
                }
            }

            private fun isExcluded(memberConfigs: List<CwtMemberConfig<*>>): Boolean {
                return memberConfigs.all { it.configExpression.type in CwtDataTypeGroups.PathReference }
            }

            private fun isIgnoredByConfigs(element: ParadoxScriptExpressionElement, memberConfigs: List<CwtMemberConfig<*>>): Boolean {
                if (!ignoredByConfigs) return false
                val value = element.value
                for (memberConfig in memberConfigs) {
                    val configExpression = memberConfig.configExpression
                    if (configExpression.type in CwtDataTypeGroups.DefinitionAware) {
                        val definitionType = configExpression.value ?: continue
                        val configs = configGroup.extendedDefinitions.findFromPattern(value, element, configGroup).orEmpty()
                        val config = configs.find { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionType) }
                        if (config != null) return true
                        if (definitionType == ParadoxDefinitionTypes.GameRule) {
                            val config1 = configGroup.extendedGameRules.findFromPattern(value, element, configGroup)
                            if (config1 != null) return true
                        }
                        if (definitionType == ParadoxDefinitionTypes.OnAction) {
                            val config1 = configGroup.extendedOnActions.findFromPattern(value, element, configGroup)
                            if (config1 != null) return true
                        }
                    }
                }
                return false
            }

            private fun continueCheck(configs: List<CwtMemberConfig<*>>): Boolean {
                //any规则不需要再向下检查
                if (configs.any { it.configExpression.type == CwtDataTypes.Any }) return false
                return true
            }

            private fun getFixes(element: PsiElement, expectedConfigs: List<CwtMemberConfig<*>>): List<LocalQuickFix> {
                return buildList {
                    val expressionElement = when (element) {
                        is ParadoxScriptProperty -> element.propertyKey
                        is ParadoxScriptStringExpressionElement -> element
                        else -> null
                    }
                    if (expressionElement != null) {
                        val locales = ParadoxLocaleManager.getLocaleConfigs()
                        val context = expectedConfigs.firstNotNullOfOrNull {
                            ParadoxLocalisationCodeInsightContextBuilder.fromReference(expressionElement, it, locales, fromInspection = true)
                        }
                        if (context != null) {
                            this += GenerateLocalisationsFix(expressionElement, context)
                            this += GenerateLocalisationsInFileFix(expressionElement)
                        }
                    }
                }
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
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

