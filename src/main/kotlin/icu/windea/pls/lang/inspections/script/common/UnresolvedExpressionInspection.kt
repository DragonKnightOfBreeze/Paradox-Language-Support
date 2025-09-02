package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.isAncestor
import com.intellij.psi.util.parentOfType
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configContext.CwtConfigContext
import icu.windea.pls.config.configContext.isDefinition
import icu.windea.pls.config.configContext.isDefinitionOrMember
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.extendedDefinitions
import icu.windea.pls.config.configGroup.extendedGameRules
import icu.windea.pls.config.configGroup.extendedOnActions
import icu.windea.pls.config.findFromPattern
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.truncate
import icu.windea.pls.ep.config.CwtOverriddenConfigProvider
import icu.windea.pls.lang.codeInsight.expression
import icu.windea.pls.lang.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.inspections.disabledElement
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.quickfix.GenerateLocalisationsFix
import icu.windea.pls.lang.quickfix.GenerateLocalisationsInFileFix
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.PlsVfsManager
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContextBuilder
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isExpression
import icu.windea.pls.script.psi.tagType
import javax.swing.JComponent

/**
 * 无法解析的表达式的检查。
 *
 * @property ignoredByConfigs （配置项）如果对应的扩展的CWT规则存在，是否需要忽略此代码检查。
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles 是否在内联脚本文件中忽略此代码检查。
 */
class UnresolvedExpressionInspection : LocalInspectionTool() {
    @JvmField
    var showExpectInfo = true
    @JvmField
    var ignoredByConfigs = false
    @JvmField
    var ignoredInInjectedFiles = false
    @JvmField
    var ignoredInInlineScriptFiles = false

    //如果一个表达式（属性/值）无法解析，需要跳过直接检测下一个表达式，而不是继续向下检查它的子节点

    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (ignoredInInjectedFiles && PlsVfsManager.isInjectedFile(file.virtualFile)) return false
        if (ignoredInInlineScriptFiles && ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return false
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        val file = holder.file
        val project = holder.project
        val configGroup = PlsFacade.getConfigGroup(project, selectGameType(file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                val result = when (element) {
                    is ParadoxScriptProperty -> visitProperty(element)
                    is ParadoxScriptValue -> visitValue(element)
                    else -> true
                }
                if (!result) session.disabledElement = element
            }

            private fun visitProperty(element: ParadoxScriptProperty): Boolean {
                val disabledElement = session.disabledElement
                if (disabledElement != null && disabledElement.isAncestor(element)) return true

                //skip checking property if property key is parameterized
                val propertyKey = element.propertyKey
                if (propertyKey.text.isParameterized()) return false

                //NOTE if code is skipped by following checks, it may still be unresolved in fact, should be optimized in the future

                //skip if config context not exists
                val configContext = ParadoxExpressionManager.getConfigContext(element) ?: return true
                //skip if config context is not suitable
                if (!configContext.isDefinitionOrMember() || configContext.isDefinition()) return true
                //skip if there are no context configs
                if (configContext.getConfigs().isEmpty()) return true

                val configs = ParadoxExpressionManager.getConfigs(element)
                if (configs.isEmpty()) {
                    val expectedConfigs = getExpectedConfigs(element)
                    if (expectedConfigs.isNotEmpty()) {
                        //判断是否需要排除
                        if (isExcluded(expectedConfigs)) return true
                        //判断是否需要忽略
                        if (isIgnoredByConfigs(propertyKey, expectedConfigs)) return true
                    }
                    val expectedExpressions = expectedConfigs.mapTo(mutableSetOf()) { it.configExpression.expressionString }
                    val expect = if (showExpectInfo) expectedExpressions.truncate(PlsFacade.getInternalSettings().itemLimit).joinToString() else null
                    val message = when {
                        expect == null -> PlsBundle.message("inspection.script.unresolvedExpression.desc.1.1", propertyKey.expression)
                        expect.isNotEmpty() -> PlsBundle.message("inspection.script.unresolvedExpression.desc.1.2", propertyKey.expression, expect)
                        else -> PlsBundle.message("inspection.script.unresolvedExpression.desc.1.3", propertyKey.expression)
                    }
                    val highlightType = getHighlightType(propertyKey, expectedConfigs)
                    val fixes = getFixes(propertyKey, expectedConfigs)
                    holder.registerProblem(element, message, highlightType, *fixes.toTypedArray())
                    //skip checking children
                    return false
                }
                return continueCheck(configs)
            }

            private fun visitValue(element: ParadoxScriptValue): Boolean {
                if (!element.isExpression()) return false // skip check if element is not an expression

                val disabledElement = session.disabledElement
                if (disabledElement != null && disabledElement.isAncestor(element)) return true

                //skip checking value if it is parameterized
                if (element is ParadoxScriptString && element.text.isParameterized()) return false
                if (element is ParadoxScriptScriptedVariableReference && element.text.isParameterized()) return false

                //NOTE if code is skipped by following checks, it may still be unresolved in fact, should be optimized in the future

                //skip if config context not exists
                val configContext = ParadoxExpressionManager.getConfigContext(element) ?: return true
                //skip if config context is not suitable
                if (!configContext.isDefinitionOrMember()) return true
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
                    val expect = if (showExpectInfo) expectedExpressions.truncate(PlsFacade.getInternalSettings().itemLimit).joinToString() else null
                    val message = when {
                        expect == null -> PlsBundle.message("inspection.script.unresolvedExpression.desc.2.1", element.expression)
                        expect.isNotEmpty() -> PlsBundle.message("inspection.script.unresolvedExpression.desc.2.2", element.expression, expect)
                        else -> PlsBundle.message("inspection.script.unresolvedExpression.desc.2.3", element.expression)
                    }
                    val highlightType = getHighlightType(element, expectedConfigs)
                    val fixes = getFixes(element, expectedConfigs)
                    holder.registerProblem(element, message, highlightType, *fixes.toTypedArray())
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

            private fun getHighlightType(expressionElement: ParadoxScriptExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): ProblemHighlightType {
                if (expressionElement !is ParadoxScriptStringExpressionElement) return ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                return when {
                    expectedConfigs.any { it.configExpression.type in CwtDataTypeGroups.TextReference } -> ProblemHighlightType.WEAK_WARNING // use weak warning (wave lines) instead
                    else -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                }
            }

            private fun getFixes(expressionElement: ParadoxScriptExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): List<LocalQuickFix> {
                if (expressionElement !is ParadoxScriptStringExpressionElement) return emptyList()
                return buildList {
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

            private fun continueCheck(configs: List<CwtMemberConfig<*>>): Boolean {
                //any规则不需要再向下检查
                if (configs.any { it.configExpression.type == CwtDataTypes.Any }) return false
                return true
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
            //ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles)
                    .actionListener { _, component -> ignoredInInjectedFiles = component.isSelected }
            }
            //ignoredInInlineScriptFiles
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInlineScriptFiles"))
                    .bindSelected(::ignoredInInlineScriptFiles)
                    .actionListener { _, component -> ignoredInInlineScriptFiles = component.isSelected }
            }
        }
    }
}

