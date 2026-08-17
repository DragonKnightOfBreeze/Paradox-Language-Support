package icu.windea.pls.ep.resolve.modifier

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.icon
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionLookupProvider
import icu.windea.pls.lang.codeInsight.completion.addToResult
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.util.ParadoxModifierProcessor
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxModificationTrackers
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModifierInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.economicCategoryInfo
import icu.windea.pls.model.economicCategoryModifierInfo
import icu.windea.pls.model.modifierConfig
import icu.windea.pls.model.templateExpression
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 提供对预定义的修正的支持。
 *
 * 示例：`pop_happiness`（来自 `modifiers.cwt`）
 */
class ParadoxPredefinedModifierSupport : ParadoxModifierSupport {
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        return configGroup.predefinedModifiers[modifierName] != null
    }

    override fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        val modifierName = name
        val modifierConfig = configGroup.predefinedModifiers[modifierName] ?: return null
        val modifierInfo = ParadoxModifierInfo(modifierName, configGroup.project, configGroup.gameType)
        modifierInfo.modifierConfig = modifierConfig
        return modifierInfo
    }

    override fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet, modifierNames: MutableSet<@CaseInsensitive String>) {
        val element = context.contextElement
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = context.configGroup
        val scopeContext = context.scopeContext
        val completeOnlyScopeIsMatched = ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched

        ParadoxModifierProcessor.processPredefinedModifierConfig(configGroup) p@{ modifierConfig ->
            // 排除重复的
            if (!modifierNames.add(modifierConfig.name)) return@p true

            // 排除不匹配 modifier 的 supported_scopes 的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
            if (!scopeMatched && completeOnlyScopeIsMatched) return@p true

            val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, modifierConfig.config, withConfigExpression = false)
            val template = modifierConfig.template
            if (template.expressionString.isNotEmpty()) return@p true
            val typeFile = modifierConfig.pointer.containingFile
            val typeText = typeFile?.name
            val typeIcon = typeFile?.icon
            val name = modifierConfig.name
            val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this) ?: return@p true
            val lookupElement = ParadoxCompletionLookupProvider.fromModifier(context, modifierElement, typeText, typeIcon, hintText)
            lookupElement.addToResult(context, result)
        }
    }

    override fun processModifier(element: PsiElement, configGroup: CwtConfigGroup, processor: Processor<ParadoxModifierLightElement>): Boolean {
        return ParadoxModifierProcessor.processPredefinedModifierConfig(configGroup) p@{ modifierConfig ->
            val name = modifierConfig.name
            val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this) ?: return@p true
            processor.process(modifierElement)
        }
    }

    override fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker {
        return ModificationTracker.NEVER_CHANGED
    }
}

/**
 * 提供对从模板表达式生成的修正的支持。这些模板表达式使用特殊的匹配逻辑（忽略大小写）。
 *
 * 作为来源的模板表达式仅支持使用以下数据类型的动态片段：
 * - [CwtDataTypes.Definition]
 * - [CwtDataTypes.EnumValue]
 * - [CwtDataTypes.Value]
 *
 * 示例：`job_researcher_add`（来自 `modifiers.cwt` 中的 `job_<job>_add`）
 */
class ParadoxTemplateModifierSupport : ParadoxModifierSupport {
    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        var matched = false
        ParadoxModifierProcessor.processGeneratedModifierConfig(configGroup) p@{ modifierConfig ->
            val templateExpression = ParadoxTemplateExpression.resolve(modifierName, null, configGroup, modifierConfig)
            if (templateExpression == null) return@p true
            matched = true
            false
        }
        return matched
    }

    override fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        // NOTE 2.1.8 如果存在多个非精确匹配的候选项，需要检查是否精确匹配，或者回退为第一个
        val modifierName = name
        val gameType = configGroup.gameType
        val project = configGroup.project
        val modifierInfoCandidates = mutableListOf<ParadoxModifierInfo>()
        for (modifierConfig in configGroup.generatedModifiers.values) {
            ProgressManager.checkCanceled()
            val templateExpression = ParadoxTemplateExpression.resolve(modifierName, null, configGroup, modifierConfig) ?: continue
            val modifierInfo = ParadoxModifierInfo(modifierName, project, gameType)
            modifierInfo.modifierConfig = modifierConfig
            modifierInfo.templateExpression = templateExpression
            if (templateExpression.isExactMatched()) return modifierInfo
            modifierInfoCandidates += modifierInfo
        }
        if (modifierInfoCandidates.isEmpty()) return null
        return modifierInfoCandidates.singleOrNull()
            ?: modifierInfoCandidates.find { it.templateExpression!!.checkExactMatched(element) }
            ?: modifierInfoCandidates.firstOrNull()
    }

    override fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet, modifierNames: MutableSet<@CaseInsensitive String>) {
        val element = context.contextElement
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = context.configGroup
        val scopeContext = context.scopeContext
        val completeOnlyScopeIsMatched = ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched

        ParadoxModifierProcessor.processGeneratedModifierConfig(configGroup) p@{ modifierConfig ->
            // 排除不匹配 modifier 的 supported_scopes 的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
            if (!scopeMatched && completeOnlyScopeIsMatched) return@p true

            val typeFile = modifierConfig.pointer.containingFile
            val typeText = typeFile?.name
            val typeIcon = typeFile?.icon
            val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, modifierConfig.config, withConfigExpression = true)
            // 生成的 modifier
            ParadoxModifierProcessor.processModifierTemplate(element, configGroup, modifierConfig.template) p1@{ name ->
                // 排除重复的
                if (!modifierNames.add(modifierConfig.name)) return@p1 true

                val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this) ?: return@p1 true
                val lookupElement = ParadoxCompletionLookupProvider.fromModifier(context, modifierElement, typeText, typeIcon, hintText)
                lookupElement.addToResult(context, result)
            }
        }
    }

    override fun processModifier(element: PsiElement, configGroup: CwtConfigGroup, processor: Processor<ParadoxModifierLightElement>): Boolean {
        return ParadoxModifierProcessor.processGeneratedModifierConfig(configGroup) p@{ modifierConfig ->
            ParadoxModifierProcessor.processModifierTemplate(element, configGroup, modifierConfig.template) p1@{ name ->
                val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this) ?: return@p1 true
                processor.process(modifierElement)
            }
        }
    }

    override fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker {
        // TODO 可以进一步缩小范围
        return ParadoxModificationTrackers.scriptFileFromFilePathPatterns("**/*.txt")
    }
}

/**
 * （仅限 Stellaris）提供对从经济分类（`economic_category`）生成的修正的支持。这些修正使用特殊的生成逻辑。
 *
 * 示例：`country_base_energy_produces_add`（来自经济分类 `country_base`）
 */
@ForGameType(ParadoxGameType.Stellaris)
class ParadoxEconomicCategoryModifierSupport : ParadoxModifierSupport {
    override fun supports(gameType: ParadoxGameType) = gameType == ParadoxGameType.Stellaris

    override fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val modifierName = name
        var matched = false
        ParadoxModifierProcessor.processEconomicCategoryInfo(element, configGroup) p@{ economicCategoryInfo ->
            economicCategoryInfo.modifiers.process p1@{ economicCategoryModifierInfo ->
                if (!economicCategoryModifierInfo.name.equals(modifierName, ignoreCase = true)) return@p1 true
                matched = true
                false
            }
        }
        return matched
    }

    override fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
        val modifierName = name
        var result: ParadoxModifierInfo? = null
        ParadoxModifierProcessor.processOrderedEconomicCategoryInfo(element, configGroup) p@{ economicCategoryInfo ->
            economicCategoryInfo.modifiers.process p1@{ economicCategoryModifierInfo ->
                if (!economicCategoryModifierInfo.name.equals(modifierName, ignoreCase = true)) return@p1 true
                val modifierInfo = ParadoxModifierInfo(modifierName, configGroup.project, configGroup.gameType)
                modifierInfo.economicCategoryInfo = economicCategoryInfo
                modifierInfo.economicCategoryModifierInfo = economicCategoryModifierInfo
                result = modifierInfo
                false
            }
        }
        return result
    }

    override fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet, modifierNames: MutableSet<@CaseInsensitive String>) {
        val element = context.contextElement
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = context.configGroup
        val scopeContext = context.scopeContext
        val completeOnlyScopeIsMatched = ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched

        ParadoxModifierProcessor.processEconomicCategoryInfo(element, configGroup) p@{ economicCategoryInfo ->
            // 排除不匹配 modifier 的 supported_scopes 的情况
            val modifierCategories = ParadoxConfigManager.getModifierCategory(economicCategoryInfo.modifierCategory, configGroup)
            val supportedScopes = ParadoxScopeManager.getSupportedScopes(modifierCategories)
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, supportedScopes, configGroup)
            if (!scopeMatched && completeOnlyScopeIsMatched) return@p true

            val typeText = economicCategoryInfo.name
            val typeIcon = ChronicleIcons.Nodes.Definition(ParadoxDefinitionTypes.economicCategory)
            val hintText = " from economic category " + economicCategoryInfo.name
            economicCategoryInfo.modifiers.process p1@{ economicCategoryModifierInfo ->
                val name = economicCategoryModifierInfo.name
                if (!modifierNames.add(name)) return@p1 true // 排除重复的
                val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this) ?: return@p1 true
                val lookupElement = ParadoxCompletionLookupProvider.fromModifier(context, modifierElement, typeText, typeIcon, hintText)
                lookupElement.addToResult(context, result)
            }
        }
    }

    override fun processModifier(element: PsiElement, configGroup: CwtConfigGroup, processor: Processor<ParadoxModifierLightElement>): Boolean {
        return ParadoxModifierProcessor.processEconomicCategoryInfo(element, configGroup) p@{ economicCategoryInfo ->
            economicCategoryInfo.modifiers.process p1@{ economicCategoryModifierInfo ->
                val name = economicCategoryModifierInfo.name
                val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup, this) ?: return@p1 true
                processor.process(modifierElement)
            }
        }
    }

    override fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker {
        return ParadoxModificationTrackers.scriptFileFromFilePathPatterns("common/economic_categories/**/*.txt")
    }
}
