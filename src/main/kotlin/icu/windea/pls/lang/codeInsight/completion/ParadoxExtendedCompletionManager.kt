package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.match.matchesByPattern
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression

object ParadoxExtendedCompletionManager {
    fun completeExtendedScriptedVariable(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!ChronicleSettings.getInstance().state.completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val configGroup = context.configGroup
        val icon = ChronicleIcons.Configs.ExtendedScriptedVariable
        configGroup.extendedScriptedVariables.values.forEach f@{ extendedConfig ->
            ProgressManager.checkCanceled()
            val name = extendedConfig.name
            if (checkExtendedConfigName(name)) return@f
            ParadoxCompletionLookupProvider.forExtendedConfig(extendedConfig, name, icon).addToResult(context, result)
        }
    }

    fun completeExtendedDefinition(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!ChronicleSettings.getInstance().state.completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        // `context.config` can be either declaration (`type = {...}`) or reference (`<type>`)

        val contextConfig = context.config ?: return
        val configGroup = contextConfig.configGroup
        val config = when (contextConfig) {
            is CwtPropertyConfig -> CwtValueConfig.createMock(configGroup, "<${contextConfig.key}>")
            else -> contextConfig
        }
        val typeExpression = config.configExpression?.metadata?.value ?: return
        val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, config)
        run r1@{
            val icon = ChronicleIcons.Configs.ExtendedDefinition
            configGroup.extendedDefinitions.values.forEach { extendedConfigs ->
                extendedConfigs.forEach f@{ extendedConfig ->
                    ProgressManager.checkCanceled()
                    val name = extendedConfig.name
                    if (name.isEmpty()) return@f
                    if (checkExtendedConfigName(name)) return@f
                    val type = extendedConfig.type
                    if (!ParadoxDefinitionTypeExpression.resolve(type).matches(typeExpression)) return@f
                    ParadoxCompletionLookupProvider.fromExtendedConfig(context, extendedConfig, name, icon, hintText).addToResult(context, result)
                }
            }
        }
        run r1@{
            if (typeExpression != ParadoxDefinitionTypes.gameRule) return@r1
            val icon = ChronicleIcons.Configs.ExtendedGameRule
            configGroup.extendedGameRules.values.forEach f@{ extendedConfig ->
                ProgressManager.checkCanceled()
                val name = extendedConfig.name
                if (checkExtendedConfigName(name)) return@f
                ParadoxCompletionLookupProvider.fromExtendedConfig(context, extendedConfig, name, icon, hintText).addToResult(context, result)
            }
        }
        run r1@{
            if (typeExpression != ParadoxDefinitionTypes.onAction) return@r1
            val icon = ChronicleIcons.Configs.ExtendedOnAction
            configGroup.extendedOnActions.values.forEach f@{ extendedConfig ->
                ProgressManager.checkCanceled()
                val name = extendedConfig.name
                if (checkExtendedConfigName(name)) return@f
                ParadoxCompletionLookupProvider.fromExtendedConfig(context, extendedConfig, name, icon, hintText).addToResult(context, result)
            }
        }
    }

    fun completeExtendedParameter(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!ChronicleSettings.getInstance().state.completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val configGroup = context.configGroup
        val contextKey = context.contextKey ?: return
        val argumentNames = context.argumentNames
        val contextElement = context.contextElement
        val icon = ChronicleIcons.Configs.ExtendedParameter
        configGroup.extendedParameters.values.forEach { extendedConfigs ->
            extendedConfigs.forEach f@{ extendedConfig ->
                if (!extendedConfig.contextKey.matchesByPattern(contextKey, contextElement, configGroup)) return@f
                val name = extendedConfig.name
                if (checkExtendedConfigName(name)) return@f
                if (argumentNames != null && !argumentNames.add(name)) return@f  // 排除已输入的
                ParadoxCompletionLookupProvider.fromExtendedConfig(context, extendedConfig, name, icon).addToResult(context, result)
            }
        }
    }

    fun completeExtendedComplexEnumValue(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!ChronicleSettings.getInstance().state.completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config ?: return
        val enumName = config.configExpression?.metadata?.value ?: return
        val configGroup = config.configGroup
        val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, config)
        val icon = ChronicleIcons.Configs.ExtendedComplexEnumValue
        configGroup.extendedComplexEnumValues[enumName]?.values?.forEach f@{ extendedConfig ->
            ProgressManager.checkCanceled()
            val name = extendedConfig.name
            if (checkExtendedConfigName(name)) return@f
            ParadoxCompletionLookupProvider.fromExtendedConfig(context, extendedConfig, name, icon, hintText).addToResult(context, result)
        }
    }

    fun completeExtendedDynamicValue(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!ChronicleSettings.getInstance().state.completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config
        val configs = context.configs
        val finalConfigs = configs.ifEmpty { config.to.singletonListOrEmpty() }
        if (finalConfigs.isEmpty()) return
        for (config in finalConfigs) {
            val dynamicValueType = config.configExpression?.metadata?.value ?: continue
            val configGroup = config.configGroup
            val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, config)

            val icon = ChronicleIcons.Nodes.DynamicValue(dynamicValueType)
            configGroup.extendedDynamicValues[dynamicValueType]?.values?.forEach f@{ extendedConfig ->
                ProgressManager.checkCanceled()
                val name = extendedConfig.name
                if (checkExtendedConfigName(name)) return@f
                ParadoxCompletionLookupProvider.fromExtendedConfig(context, extendedConfig, name, icon, hintText).addToResult(context, result)
            }
        }
    }

    fun completeExtendedInlineScript(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!ChronicleSettings.getInstance().state.completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config ?: return
        val configGroup = config.configGroup
        val hintText = ParadoxCompletionLookupProvider.getConfigBasedHintText(context, config)
        val icon = ChronicleIcons.Configs.ExtendedInlineScript
        configGroup.extendedInlineScripts.values.forEach f@{ extendedConfig ->
            ProgressManager.checkCanceled()
            val name = extendedConfig.name
            if (checkExtendedConfigName(name)) return@f
            ParadoxCompletionLookupProvider.fromExtendedConfig(context, extendedConfig, name, icon, hintText).addToResult(context, result)
        }
    }

    private val ignoredCharsForExtendedConfigName = ".:<>[]".toCharArray()

    private fun checkExtendedConfigName(text: String): Boolean {
        // ignored if config name is empty
        if (text.isEmpty()) return true
        // ignored if config name is a template expression, ant expression or regex
        if (text.any { it in ignoredCharsForExtendedConfigName }) return true
        return false
    }
}
