package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.patterns.StandardPatterns
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.resolved
import icu.windea.pls.config.processUnionCandidates
import icu.windea.pls.core.codeInsight.LimitedCompletionProcessor
import icu.windea.pls.core.icon
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.ep.resolve.expression.ParadoxPathReferenceExpressionSupport
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
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
        val nextScopeMatched = ParadoxCompletionUtil.isNextScopeMatched(context)
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

    // endregion

    // region General Completion Methods

    fun completeLocalisation(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config ?: return

        // 优化：如果已经输入的关键词不是合法的本地化的名字，不要尝试进行本地化的代码补全
        if (context.keyword.isNotEmpty() && !ParadoxNameValidators.checkLocalisationName(context.keyword)) return

        // 本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(context.keyword.length))

        val tailText = ParadoxCompletionUtil.getPatchableTailText(context, config)
        val selector = ParadoxLocalisationSearch.selector(context.project, context.contextElement)
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> p@{ localisation ->
            // apply extraFilter since it's necessary
            if (context.extraFilter?.invoke(localisation) == false) return@p true

            val name = localisation.name // =localisation.paradoxLocalisationInfo?.name
            val typeFile = localisation.containingFile
            val lookupElement = LookupElementBuilder.create(localisation, name)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withPatchableIcon(ChronicleIcons.Nodes.Localisation)
                .withPatchableTailText(tailText)
                .forExpression(context)
            result.addElement(lookupElement, context)
            true
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

        val tailText = ParadoxCompletionUtil.getPatchableTailText(context, config)
        val selector = ParadoxLocalisationSearch.selector(context.project, context.contextElement)
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> p@{ syncedLocalisation ->
            // apply extraFilter since it's necessary
            if (context.extraFilter?.invoke(syncedLocalisation) == false) return@p true

            val name = syncedLocalisation.name // =localisation.paradoxLocalisationInfo?.name
            val typeFile = syncedLocalisation.containingFile
            val lookupElement = LookupElementBuilder.create(syncedLocalisation, name)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withPatchableIcon(ChronicleIcons.Nodes.Localisation)
                .withPatchableTailText(tailText)
                .forExpression(context)
            result.addElement(lookupElement, context)
            true
        }
        // 保证索引在此 readAction 中可用
        runSmartReadAction(context.project, inSmartMode = true) {
            ParadoxLocalisationSearch.processVariantsSynced(result.prefixMatcher, selector, processor)
        }
    }

    fun completeDefinition(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val scopeContext = context.scopeContext
        val typeExpression = config.configExpression?.value ?: return
        val configGroup = config.configGroup
        val tailText = ParadoxCompletionUtil.getPatchableTailText(context, config)
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

            val name = definitionInfo.name
            val typeFile = definition.containingFile
            val lookupElement = LookupElementBuilder.create(definition, name)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withPatchableIcon(ChronicleIcons.Nodes.Definition(definitionInfo.type))
                .withPatchableTailText(tailText)
                .withScopeMatched(scopeMatched)
                .withDefinitionLocalizedNamesIfNecessary(definition)
                .forExpression(context)
            result.addElement(lookupElement, context)
            true
        }

        ParadoxExtendedCompletionManager.completeExtendedDefinition(context, result)
    }

    fun completePathReference(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val configExpression = config.configExpression ?: return
        val pathReferenceExpressionSupport = ParadoxPathReferenceExpressionSupport.get(configExpression)
        if (pathReferenceExpressionSupport != null) {
            val tailText = ParadoxCompletionUtil.getPatchableTailText(context, config)
            val fileExtensions = when (config) {
                is CwtMemberConfig<*> -> config.optionData.fileExtensions.orEmpty()
                else -> emptySet()
            }
            // 仅提示匹配 `file_extensions` 选项指定的扩展名的，如果存在
            val selector = ParadoxFilePathSearch.selector(context.project, context.contextElement).contextSensitive().distinct()
                .withFileExtensions(fileExtensions)
            ParadoxFilePathSearch.search(null, configExpression, selector).processAsync p@{ virtualFile ->
                ProgressManager.checkCanceled()
                val file = virtualFile.toPsiFile(context.project) ?: return@p true
                val filePath = virtualFile.fileInfo?.path?.path ?: return@p true
                val name = pathReferenceExpressionSupport.extract(configExpression, context.file, filePath) ?: return@p true
                val lookupElement = LookupElementBuilder.create(file, name)
                    .withTypeText(file.name, file.icon, true)
                    .withPatchableIcon(ChronicleIcons.Nodes.PathReference(config.configExpression))
                    .withPatchableTailText(tailText)
                    .forExpression(context)
                result.addElement(lookupElement, context)
                true
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
        val enumName = config.configExpression?.value ?: return
        val tailText = ParadoxCompletionUtil.getPatchableTailText(context, config)
        val enumConfig = configGroup.enums[enumName] ?: return
        val enumValueConfigs = enumConfig.valueConfigMap.values
        if (enumValueConfigs.isEmpty()) return
        val typeFile = enumConfig.pointer.containingFile
        for (enumValueConfig in enumValueConfigs) {
            val name = enumValueConfig.value
            val element = enumValueConfig.pointer.element ?: continue
            val lookupElement = LookupElementBuilder.create(element, name)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false)
                .withPriority(ChronicleCompletionPriorities.enumValue)
                .withPatchableIcon(ChronicleIcons.Nodes.EnumValue)
                .withPatchableTailText(tailText)
                .forExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeComplexEnumValue(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val config = context.config ?: return
        val enumName = config.configExpression?.value ?: return
        val tailText = ParadoxCompletionUtil.getPatchableTailText(context, config)
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
            val lookupElement = LookupElementBuilder.create(element, name)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(!complexEnumConfig.caseInsensitive) // # 261
                .withPriority(ChronicleCompletionPriorities.complexEnumValue)
                .withPatchableIcon(ChronicleIcons.Nodes.ComplexEnumValue(enumName))
                .withPatchableTailText(tailText)
                .forExpression(context)
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeCsvUnionValue(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val config = context.config ?: return
        val unionName = config.configExpression?.value ?: return
        val unionConfig = configGroup.unions[unionName] ?: return
        unionConfig.processUnionCandidates { valueConfig ->
            val context = context.copy(config = valueConfig, configs = setOf(valueConfig))
            completeCsvExpression(context, result)
            true
        }
    }

    fun completeScriptUnionValue(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val config = context.config ?: return
        val unionName = config.configExpression?.value ?: return
        val unionConfig = configGroup.unions[unionName] ?: return
        unionConfig.processUnionCandidates { valueConfig ->
            val context = context.copy(config = valueConfig, configs = setOf(valueConfig))
            completeScriptExpression(context, result)
            true
        }
    }

    fun completeDynamicValue(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config
        val configs = context.configs
        val finalConfigs = configs.ifEmpty { config.to.singletonListOrEmpty() }
        if (finalConfigs.isEmpty()) return
        for (finalConfig in finalConfigs) {
            completePredefinedDynamicValue(context, result, finalConfig)
            completeIndexedDynamicValue(context, result, finalConfig)
        }
        ParadoxExtendedCompletionManager.completeExtendedDynamicValue(context, result)
    }

    fun completePredefinedDynamicValue(context: ParadoxCompletionContext, result: CompletionResultSet, config: CwtConfig<*>) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val configExpression = config.configExpression ?: return
        val dynamicValueType = configExpression.value ?: return
        if (configExpression.type != CwtDataTypes.Value && configExpression.type != CwtDataTypes.DynamicValue) return
        val tailText = ParadoxCompletionUtil.getPatchableTailText(context, config)
        val valueConfig = configGroup.dynamicValueTypes[dynamicValueType] ?: return
        val dynamicValueTypeConfigs = valueConfig.valueConfigMap.values
        for (dynamicValueTypeConfig in dynamicValueTypeConfigs) {
            val name = dynamicValueTypeConfig.value
            val element = dynamicValueTypeConfig.pointer.element ?: continue
            val typeFile = valueConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPatchableIcon(ChronicleIcons.Nodes.DynamicValue(dynamicValueType))
                .withPatchableTailText(tailText)
                .forExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeIndexedDynamicValue(context: ParadoxCompletionContext, result: CompletionResultSet, config: CwtConfig<*>) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val configExpression = config.configExpression ?: return
        val dynamicValueType = configExpression.value ?: return
        val tailText = " by $configExpression"
        val selector = ParadoxDynamicValueSearch.selector(configGroup.project, context.contextElement).distinct()
        ParadoxDynamicValueSearch.search(null, dynamicValueType, selector).processAsync p@{ info ->
            ProgressManager.checkCanceled()
            val name = info.name
            if (name == context.keyword) return@p true // 排除和当前输入的同名的
            val readWriteAccess = info.readWriteAccess
            val element = ParadoxDynamicValueLightElement(context.contextElement, name, dynamicValueType, readWriteAccess, configGroup.gameType, configGroup.project)
            val lookupElement = LookupElementBuilder.create(element, name)
                .withPatchableIcon(ChronicleIcons.Nodes.DynamicValue(dynamicValueType))
                .withPatchableTailText(tailText)
                .forExpression(context)
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeAliasName(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup
        val config = context.config ?: return
        val aliasName = config.configExpression?.value ?: return
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return
        for (aliasConfigs in aliasGroup.values) {
            val context = context.copy(config = aliasConfigs.first(), configs = aliasConfigs)
            completeScriptExpression(context, result)
        }
    }

    fun completeConstant(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val config = context.config ?: return
        val configExpression = config.configExpression ?: return
        val icon = when {
            configExpression.isKey -> ChronicleIcons.Nodes.Property
            else -> ChronicleIcons.Nodes.Value
        }
        val name = configExpression.value ?: return
        if (!configExpression.isKey) {
            // 常量的值也可能是yes/no
            if (name == "yes") {
                if (context.quoted) return
                result.addElement(ChronicleLookupElements.yesLookupElement, context)
                return
            }
            if (name == "no") {
                if (context.quoted) return
                result.addElement(ChronicleLookupElements.noLookupElement, context)
                return
            }
        }
        val element = config.resolved().pointer.element ?: return
        val typeFile = config.resolved().pointer.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false)
            .withPriority(ChronicleCompletionPriorities.constant)
            .withPatchableIcon(icon)
            .withScopeMatched(context.scopeMatched)
            .forExpression(context)
        result.addElement(lookupElement, context)
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
        val tailText = " by $configExpression"
        val selector = ParadoxShaderEffectSearch.selector(configGroup.project, context.contextElement).distinct()
        ParadoxShaderEffectSearch.search(null, selector).processAsync p@{ info ->
            ProgressManager.checkCanceled()
            val name = info.name
            if (name == context.keyword) return@p true // 排除和当前输入的同名的
            val element = ParadoxShaderEffectLightElement(context.contextElement, name, configGroup.gameType, configGroup.project)
            val lookupElement = LookupElementBuilder.create(element, name)
                .withPatchableIcon(ChronicleIcons.Nodes.ShaderEffect)
                .withPatchableTailText(tailText)
                .forExpression(context)
            result.addElement(lookupElement, context)
            true
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
        val tailText = " by $configExpression"
        val selector = ParadoxMeshLocatorSearch.selector(configGroup.project, context.contextElement).distinct()
        ParadoxMeshLocatorSearch.search(null, selector).processAsync p@{ info ->
            ProgressManager.checkCanceled()
            val name = info.name
            if (name == context.keyword) return@p true // 排除和当前输入的同名的
            val element = ParadoxMeshLocatorLightElement(context.contextElement, name, configGroup.gameType, configGroup.project)
            val lookupElement = LookupElementBuilder.create(element, name)
                .withPatchableIcon(ChronicleIcons.Nodes.MeshLocator)
                .withPatchableTailText(tailText)
                .forExpression(context)
            result.addElement(lookupElement, context)
            true
        }
    }

    // endregion
}
