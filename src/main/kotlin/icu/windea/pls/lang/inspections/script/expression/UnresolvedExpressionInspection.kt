package icu.windea.pls.lang.inspections.script.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.isAncestor
import com.intellij.psi.util.parentOfType
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.inspections.InspectionService
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.truncate
import icu.windea.pls.lang.inspections.ParadoxInspectionService
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.resolve.inRoot
import icu.windea.pls.lang.resolve.isDeclarationRoot
import icu.windea.pls.lang.settings.ChronicleInternalSettings
import icu.windea.pls.lang.tagType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isDataExpression
import javax.swing.JComponent

/**
 * （脚本文件中的）无法解析的表达式的代码检查。
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles （配置项）是否在内联脚本文件中忽略此代码检查。
 * @property ignoredByConfigs （配置项）如果对应的扩展的规则存在，是否需要忽略此代码检查。
 */
class UnresolvedExpressionInspection : LocalInspectionTool() {
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var ignoredInInlineScriptFiles = false
    @JvmField var ignoredByConfigs = false
    @JvmField var showExpectInfo = true

    // 如果一个表达式（属性/值）无法解析，需要跳过直接检测下一个表达式，而不是继续向下检查它的子节点

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!ChronicleFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 判断是否需要忽略内联脚本文件
        if (ignoredInInlineScriptFiles && ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return false
        // 要求是可接受的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file, injectable = !ignoredInInjectedFiles)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : PsiElementVisitor() {
            private var disabledElement: PsiElement? = null

            override fun visitElement(element: PsiElement) {
                val result = when (element) {
                    is ParadoxScriptProperty -> visitProperty(element)
                    is ParadoxScriptValue -> visitValue(element)
                    else -> true
                }
                if (!result) disabledElement = element
            }

            private fun visitProperty(element: ParadoxScriptProperty): Boolean {
                ProgressManager.checkCanceled()
                val disabledElement = disabledElement
                if (disabledElement != null && disabledElement.isAncestor(element)) return true

                // skip checking property if property key is parameterized
                val propertyKey = element.propertyKey
                if (propertyKey.text.isParameterized()) return false

                // NOTE if code is skipped by following checks, it may still be unresolved in fact, should be optimized in the future

                // skip if config context not exists
                val configContext = ParadoxConfigManager.getConfigContext(element) ?: return true
                // skip if config context is not suitable
                if (!configContext.inRoot() || configContext.isDeclarationRoot()) return true
                // skip if there are no context configs
                if (configContext.getConfigs().isEmpty()) return true

                val configs = ParadoxConfigManager.getConfigs(element)
                if (configs.isNotEmpty()) return continueCheck(configs)

                val expectedConfigs = getExpectedConfigs(element)
                if (isSkipped(propertyKey, expectedConfigs)) return true
                val description = getDescription(propertyKey, expectedConfigs)
                val highlightType = getHighlightType(propertyKey, expectedConfigs)
                val fixes = getFixes(propertyKey, expectedConfigs)
                holder.registerProblem(element, description, highlightType, *fixes)

                // skip checking children if parent has problems
                return false
            }

            private fun visitValue(element: ParadoxScriptValue): Boolean {
                ProgressManager.checkCanceled()
                if (!element.isDataExpression()) return false // skip check if element is not an expression

                val disabledElement = disabledElement
                if (disabledElement != null && disabledElement.isAncestor(element)) return true

                // skip checking value if it is parameterized
                if (element is ParadoxScriptString && element.text.isParameterized()) return false
                if (element is ParadoxScriptScriptedVariableReference && element.text.isParameterized()) return false

                // NOTE if code is skipped by following checks, it may still be unresolved in fact, should be optimized in the future

                // skip if config context not exists
                val configContext = ParadoxConfigManager.getConfigContext(element) ?: return true
                // skip if config context is not suitable
                if (!configContext.inRoot()) return true
                // skip if there are no context configs
                if (configContext.getConfigs().isEmpty()) return true

                val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(fallback = false))
                if (configs.isNotEmpty()) return continueCheck(configs)
                // skip check value if it is a special tag and there are no matched configs
                if (element.tagType != null) return false

                val expectedConfigs = getExpectedConfigs(element, configContext)
                if (isSkipped(element, expectedConfigs)) return true
                val description = getDescription(element, expectedConfigs)
                val highlightType = getHighlightType(element, expectedConfigs)
                val fixes = getFixes(element, expectedConfigs)
                holder.registerProblem(element, description, highlightType, *fixes)

                // skip checking children if parent has problems
                return false
            }

            private fun continueCheck(configs: List<CwtMemberConfig<*>>): Boolean {
                // any 规则不需要再向下检查
                if (configs.any { it.configExpression.type == CwtDataTypes.Any }) return false
                return true
            }

            private fun getExpectedConfigs(element: ParadoxScriptProperty): List<CwtPropertyConfig> {
                // 这里使用合并后的子规则，即使parentProperty可以精确匹配
                val parentMemberElement = element.parentOfType<ParadoxScriptMember>() ?: return emptyList()
                val parentConfigContext = ParadoxConfigManager.getConfigContext(parentMemberElement) ?: return emptyList()
                return buildList {
                    val contextConfigs = parentConfigContext.getConfigs()
                    contextConfigs.forEach f@{ contextConfig ->
                        contextConfig.configs?.forEach f1@{ c1 ->
                            val c = c1 as? CwtPropertyConfig ?: return@f1
                            // 优先使用重载后的规则
                            val overriddenConfigs = ParadoxConfigService.getOverriddenConfigs(element, c)
                            if (overriddenConfigs.isNotEmpty()) {
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
                        val c = contextConfig as? CwtValueConfig ?: return@f
                        val overriddenConfigs = ParadoxConfigService.getOverriddenConfigs(element, c)
                        if (overriddenConfigs.isNotEmpty()) {
                            addAll(overriddenConfigs)
                        } else {
                            add(c)
                        }
                    }
                }
            }

            private fun isSkipped(element: ParadoxScriptExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): Boolean {
                if (expectedConfigs.isEmpty()) return false
                return when {
                    isExcluded(expectedConfigs) -> true
                    isIgnoredByConfigs(element, expectedConfigs) -> true
                    else -> false
                }
            }

            private fun isExcluded(memberConfigs: List<CwtMemberConfig<*>>): Boolean {
                return memberConfigs.all { it.configExpression.type in CwtDataTypeSets.PathReference }
            }

            private fun isIgnoredByConfigs(element: ParadoxScriptExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): Boolean {
                return ignoredByConfigs && expectedConfigs.any { ParadoxConfigManager.checkExtendedConfig(element, it) }
            }
        }
    }

    private fun getDescription(element: ParadoxScriptExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): String {
        val expect = when {
            expectedConfigs.isEmpty() -> ""
            showExpectInfo -> expectedConfigs.mapTo(mutableSetOf()) { it.configExpression.expressionString }
                .truncate(ChronicleInternalSettings.getInstance().itemLimit).joinToString()
            else -> null
        }
        val message = when (element) {
            is ParadoxScriptValue -> when {
                expect == null -> ChronicleBundle.message("inspection.script.unresolvedExpression.desc.2.1", element.expression)
                expect.isNotEmpty() -> ChronicleBundle.message("inspection.script.unresolvedExpression.desc.2.2", element.expression, expect)
                else -> ChronicleBundle.message("inspection.script.unresolvedExpression.desc.2.3", element.expression)
            }
            else -> when {
                expect == null -> ChronicleBundle.message("inspection.script.unresolvedExpression.desc.1.1", element.expression)
                expect.isNotEmpty() -> ChronicleBundle.message("inspection.script.unresolvedExpression.desc.1.2", element.expression, expect)
                else -> ChronicleBundle.message("inspection.script.unresolvedExpression.desc.1.3", element.expression)
            }
        }
        return message
    }

    private fun getHighlightType(element: ParadoxScriptExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): ProblemHighlightType {
        run {
            // localisation reference -> expression can be a string literal instead  -> use weaker highlight type
            if (element !is ParadoxScriptStringExpressionElement) return@run
            val r = expectedConfigs.any { it.configExpression.type in CwtDataTypeSets.LocalisationReference }
            if (r) return InspectionService.getWeakerHighlightType()
        }
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING
    }

    private fun getFixes(element: ParadoxScriptExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): Array<LocalQuickFix> {
        val result = mutableListOf<LocalQuickFix>()
        result += ParadoxInspectionService.getSimilarityBasedFixesForUnresolvedExpression(element, expectedConfigs)
        result += ParadoxInspectionService.getLocalisationReferenceFixesForUnresolvedExpression(element, expectedConfigs)
        if (result.isEmpty()) return LocalQuickFix.EMPTY_ARRAY
        return result.toTypedArray()
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredInInjectedFile
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
            // ignoredInInlineScriptFiles
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredInInlineScriptFiles"))
                    .bindSelected(::ignoredInInlineScriptFiles.toAtomicProperty())
            }
            // ignoredByConfigs
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredByConfigs"))
                    .bindSelected(::ignoredByConfigs.toAtomicProperty())
            }
            // showExpectInfo
            row {
                checkBox(ChronicleBundle.message("inspection.option.showExpectInfo"))
                    .bindSelected(::showExpectInfo.toAtomicProperty())
            }
        }
    }
}

