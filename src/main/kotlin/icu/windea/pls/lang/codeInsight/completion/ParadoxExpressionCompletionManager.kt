package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.progress.ProgressManager
import com.intellij.patterns.StandardPatterns
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.resolved
import icu.windea.pls.config.processCandidateConfigs
import icu.windea.pls.core.codeInsight.LimitedCompletionProcessor
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.ep.resolve.expression.ParadoxPathReferenceExpressionSupport
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxMeshLocatorLightElement
import icu.windea.pls.lang.psi.light.ParadoxShaderEffectLightElement
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.ParadoxScopeService
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxDynamicValueSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxMeshLocatorSearch
import icu.windea.pls.lang.search.ParadoxShaderEffectSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.search.util.withFileExtensions
import icu.windea.pls.lang.search.util.withSearchScopeType
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxNameValidators
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

object ParadoxExpressionCompletionManager {
    // region Entry Completion Methods

    fun completeScriptExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        // 要求规则表达式不为空
        val configExpression = context.config?.configExpression ?: return
        if (configExpression.expressionString.isEmpty()) return
        // 要求匹配作用域
        val nextScopeMatched = isNextScopeMatched(context)
        if (!nextScopeMatched && !ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) return
        val context = context.copy(scopeMatched = nextScopeMatched)
        ParadoxExpressionService.completeScriptExpression(context, result)
    }

    fun completeLocalisationExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        ParadoxExpressionService.completeLocalisationExpression(context, result)
    }

    fun completeCsvExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        ParadoxExpressionService.completeCsvExpression(context, result)
    }

    fun isNextScopeMatched(context: ParadoxCompletionContext): Boolean {
        if (!context.scopeMatched) return false
        val supportedScopes = when {
            context.config is CwtPropertyConfig -> context.config.optionMetadata.supportedScopes
            context.config is CwtAliasConfig -> context.config.supportedScopes
            context.config is CwtLinkConfig -> context.config.inputScopes
            else -> null
        }
        return when {
            context.scopeContext == null -> true
            else -> ParadoxScopeManager.matchesScope(context.scopeContext, supportedScopes, context.configGroup)
        }
    }

    // endregion

    // region General Completion Methods

    fun completeDefinition(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val scopeContext = context.scopeContext
        val typeExpression = config.configExpression?.metadata?.value ?: return
        val configGroup = config.configGroup
        val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, config)
        val selector = ParadoxDefinitionSearch.selector(context.project, context.contextElement).contextSensitive().distinct()
        ParadoxDefinitionSearch.searchElement(null, typeExpression, selector).processAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val definitionInfo = definition.definitionInfo ?: return@p true
            if (definitionInfo.name.isEmpty()) return@p true // skip anonymous definitions

            // apply extraFilter since it's necessary
            if (context.extraFilter?.invoke(definition) == false) return@p true

            // 排除不匹配可能存在的 `supported_scopes` 的情况
            val supportedScopes = ParadoxScopeService.getSupportedScopes(definition, definitionInfo)
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, supportedScopes, configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) return@p true
            ParadoxCompletionLookupProvider.fromDefinition(context, definition, hintText, scopeMatched).addToResult(context, result)
        }

        ParadoxExtendedCompletionManager.completeExtendedDefinition(context, result)
    }

    fun completeLocalisation(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config ?: return

        // 优化：如果已经输入的关键词不是合法的本地化的名字，不要尝试进行本地化的代码补全
        if (context.keyword.isNotEmpty() && !ParadoxNameValidators.checkLocalisationName(context.keyword)) return

        // 本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(context.keyword.length))

        val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, config)
        val selector = ParadoxLocalisationSearch.selector(context.project, context.contextElement)
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> p@{ localisation ->
            if (context.extraFilter?.invoke(localisation) == false) return@p true // apply extraFilter since it's necessary
            ParadoxCompletionLookupProvider.fromLocalisation(context, localisation, hintText).addToResult(context, result)
        }
        // 保证索引在此 readAction 中可用
        runSmartReadAction(context.project, inSmartMode = true) {
            ParadoxLocalisationSearch.processVariantsNormal(result.prefixMatcher, selector, processor)
        }
    }

    fun completeSyncedLocalisation(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config ?: return

        // 优化：如果已经输入的关键词不是合法的本地化的名字，不要尝试进行本地化的代码补全
        if (context.keyword.isNotEmpty() && !ParadoxNameValidators.checkLocalisationName(context.keyword)) return

        // 本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(context.keyword.length))

        val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, config)
        val selector = ParadoxLocalisationSearch.selector(context.project, context.contextElement)
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> p@{ localisation ->
            if (context.extraFilter?.invoke(localisation) == false) return@p true // apply extraFilter since it's necessary
            ParadoxCompletionLookupProvider.fromLocalisation(context, localisation, hintText).addToResult(context, result)
        }
        // 保证索引在此 readAction 中可用
        runSmartReadAction(context.project, inSmartMode = true) {
            ParadoxLocalisationSearch.processVariantsSynced(result.prefixMatcher, selector, processor)
        }
    }

    fun completePathReference(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val configExpression = config.configExpression ?: return
        val support = ParadoxPathReferenceExpressionSupport.get(configExpression)
        if (support != null) {
            val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, config)
            val fileExtensions = when (config) {
                is CwtMemberConfig<*> -> config.optionMetadata.fileExtensions.orEmpty()
                else -> emptySet()
            }
            // 仅提示匹配 `file_extensions` 选项指定的扩展名的，如果存在
            val selector = ParadoxFilePathSearch.selector(context.project, context.contextElement).contextSensitive().distinct()
                .withFileExtensions(fileExtensions)
            ParadoxFilePathSearch.search(null, configExpression, selector).processAsync p@{ virtualFile ->
                ParadoxCompletionLookupProvider.fromPathReference(context, config, virtualFile, support, hintText).addToResult(context, result)
            }
            if (config.configExpression == ParadoxInlineScriptManager.inlineScriptPathExpression) {
                ParadoxExtendedCompletionManager.completeExtendedInlineScript(context, result)
            }
        }
    }

    fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        ParadoxModifierManager.completeModifier(context, result)
    }

    fun completeEnumValue(context: ParadoxCompletionContext, result: CompletionResultSet) {
        completeStaticEnumValue(context, result)
        completeComplexEnumValue(context, result)
        ParadoxExtendedCompletionManager.completeExtendedComplexEnumValue(context, result)
    }

    fun completeStaticEnumValue(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val config = context.config ?: return
        val enumName = config.configExpression?.metadata?.value ?: return
        val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, config)
        val enumConfig = configGroup.enums[enumName] ?: return
        val enumValueConfigs = enumConfig.valueConfigMap.values
        if (enumValueConfigs.isEmpty()) return
        val typeFile = enumConfig.pointer.containingFile
        for (enumValueConfig in enumValueConfigs) {
            ProgressManager.checkCanceled()
            ParadoxCompletionLookupProvider.fromStaticEnumValue(context, enumValueConfig, typeFile, hintText).addToResult(context, result)
        }
    }

    fun completeComplexEnumValue(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val config = context.config ?: return
        val enumName = config.configExpression?.metadata?.value ?: return
        val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, config)
        val complexEnumConfig = configGroup.complexEnums[enumName] ?: return
        val typeFile = complexEnumConfig.pointer.containingFile
        val searchScopeType = complexEnumConfig.searchScopeType
        val selector = ParadoxComplexEnumValueSearch.selector(configGroup.project, context.contextElement).contextSensitive().distinct()
            .withSearchScopeType(searchScopeType)
        ParadoxComplexEnumValueSearch.search(null, enumName, selector).processAsync { info ->
            ProgressManager.checkCanceled()
            val name = info.name
            val readWriteAccess = Access.Write // write (declaration)
            val element = ParadoxComplexEnumValueLightElement(context.contextElement, name, enumName, readWriteAccess, configGroup.gameType, configGroup.project)
            ParadoxCompletionLookupProvider.fromComplexEnumValue(context, element, typeFile, hintText).addToResult(context, result)
        }
    }

    fun completeScriptUnionValue(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val config = context.config ?: return
        val unionName = config.configExpression?.metadata?.value ?: return
        val unionConfig = configGroup.unions[unionName] ?: return
        // NOTE 3.0.1 recursion guard is required here
        runWithRecursionGuard("scriptExpression.complete.union", unionName) {
            unionConfig.processCandidateConfigs { valueConfig ->
                val context = context.copy(config = valueConfig, configs = setOf(valueConfig))
                completeScriptExpression(context, result)
                true
            }
        }
    }

    fun completeCsvUnionValue(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val config = context.config ?: return
        val unionName = config.configExpression?.metadata?.value ?: return
        val unionConfig = configGroup.unions[unionName] ?: return
        // NOTE 3.0.1 recursion guard is required here
        runWithRecursionGuard("csvExpression.complete.union", unionName) {
            unionConfig.processCandidateConfigs { valueConfig ->
                val context = context.copy(config = valueConfig, configs = setOf(valueConfig))
                completeCsvExpression(context, result)
                true
            }
        }
    }

    fun completeDynamicValue(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config
        val configs = context.configs
        val finalConfigs = configs.ifEmpty { config.to.singletonListOrEmpty() }
        if (finalConfigs.isEmpty()) return
        for (finalConfig in finalConfigs) {
            ProgressManager.checkCanceled()
            completePredefinedDynamicValue(context, result, finalConfig)
            completeIndexedDynamicValue(context, result, finalConfig)
        }
        ParadoxExtendedCompletionManager.completeExtendedDynamicValue(context, result)
    }

    fun completePredefinedDynamicValue(context: ParadoxCompletionContext, result: CompletionResultSet, config: CwtConfig<*>) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val configExpression = config.configExpression ?: return
        val dynamicValueType = configExpression.metadata.value ?: return
        if (configExpression.type != CwtDataTypes.Value && configExpression.type != CwtDataTypes.DynamicValue) return
        val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, config)
        val dynamicValueTypeConfig = configGroup.dynamicValueTypes[dynamicValueType] ?: return
        val valueConfigs = dynamicValueTypeConfig.valueConfigMap.values
        if (valueConfigs.isEmpty()) return
        val typeFile = dynamicValueTypeConfig.pointer.containingFile
        for (valueConfig in valueConfigs) {
            ProgressManager.checkCanceled()
            ParadoxCompletionLookupProvider.fromPredefinedDynamicValue(context, valueConfig, dynamicValueType, typeFile, hintText).addToResult(context, result)
        }
    }

    fun completeIndexedDynamicValue(context: ParadoxCompletionContext, result: CompletionResultSet, config: CwtConfig<*>) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val configExpression = config.configExpression ?: return
        val dynamicValueType = configExpression.metadata.value ?: return
        val hintText = " by $configExpression"
        val selector = ParadoxDynamicValueSearch.selector(configGroup.project, context.contextElement).distinct()
        ParadoxDynamicValueSearch.search(null, dynamicValueType, selector).processAsync p@{ info ->
            ProgressManager.checkCanceled()
            val name = info.name
            if (name == context.keyword) return@p true // 排除和当前输入的同名的
            val readWriteAccess = info.readWriteAccess
            val element = ParadoxDynamicValueLightElement(context.contextElement, name, dynamicValueType, readWriteAccess, configGroup.gameType, configGroup.project)
            ParadoxCompletionLookupProvider.fromIndexedDynamicValue(context, element, hintText).addToResult(context, result)
        }
    }

    fun completeAliasName(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val config = context.config ?: return
        val aliasName = config.configExpression?.metadata?.value ?: return
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return
        // NOTE 3.0.1 recursion guard is required here
        runWithRecursionGuard("scriptExpression.complete.alias", aliasName) {
            for (aliasConfigs in aliasGroup.values) {
                val context = context.copy(config = aliasConfigs.first(), configs = aliasConfigs)
                completeScriptExpression(context, result)
            }
        }
    }

    fun completeConstant(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val config = context.config ?: return
        val configExpression = config.configExpression ?: return
        if (configExpression.type != CwtDataTypes.Constant) return
        val icon = when {
            configExpression.isKey -> ChronicleIcons.Nodes.Property
            else -> ChronicleIcons.Nodes.Value
        }
        val name = configExpression.expressionString
        if (!configExpression.isKey) {
            // 常量的值也可能是yes/no
            if (name == "yes") {
                if (context.leftQuoted) return
                ParadoxCompletionLookupProvider.forYesKeyword().addToResult(context, result)
                return
            }
            if (name == "no") {
                if (context.leftQuoted) return
                ParadoxCompletionLookupProvider.forNoKeyword().addToResult(context, result)
                return
            }
        }
        val element = config.resolved().pointer.element ?: return
        val typeFile = config.resolved().pointer.containingFile
        ParadoxCompletionLookupProvider.fromConstant(context, name, element, typeFile, icon).addToResult(context, result)
    }

    fun completeArgument(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val config = context.config ?: return
        // 提示参数名（仅限key）
        if (context.isKey != true || config !is CwtPropertyConfig) return
        ParadoxParameterManager.completeArguments(context, result, context.contextElement)
    }

    fun completeShaderEffect(context: ParadoxCompletionContext, result: CompletionResultSet) {
        completeIndexedShaderEffect(context, result)
    }

    fun completeIndexedShaderEffect(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val config = context.config ?: return
        val configGroup = context.configGroup
        val configExpression = config.configExpression ?: return
        val hintText = " by $configExpression"
        val selector = ParadoxShaderEffectSearch.selector(configGroup.project, context.contextElement).distinct()
        ParadoxShaderEffectSearch.search(null, selector).processAsync p@{ info ->
            ProgressManager.checkCanceled()
            val name = info.name
            if (name == context.keyword) return@p true // 排除和当前输入的同名的
            val element = ParadoxShaderEffectLightElement(context.contextElement, name, configGroup.gameType, configGroup.project)
            ParadoxCompletionLookupProvider.fromIndexedExternalReference(context, element, hintText).addToResult(context, result)
        }
    }

    fun completeMeshLocator(context: ParadoxCompletionContext, result: CompletionResultSet) {
        completeIndexedMeshLocator(context, result)
    }

    fun completeIndexedMeshLocator(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val config = context.config ?: return
        val configGroup = context.configGroup
        val configExpression = config.configExpression ?: return
        val hintText = " by $configExpression"
        val selector = ParadoxMeshLocatorSearch.selector(configGroup.project, context.contextElement).distinct()
        ParadoxMeshLocatorSearch.search(null, selector).processAsync p@{ info ->
            ProgressManager.checkCanceled()
            val name = info.name
            if (name == context.keyword) return@p true // 排除和当前输入的同名的
            val element = ParadoxMeshLocatorLightElement(context.contextElement, name, configGroup.gameType, configGroup.project)
            ParadoxCompletionLookupProvider.fromIndexedExternalReference(context, element, hintText).addToResult(context, result)
        }
    }

    // endregion
}
