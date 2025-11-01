package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.extendedComplexEnumValues
import icu.windea.pls.config.configGroup.extendedDefinitions
import icu.windea.pls.config.configGroup.extendedDynamicValues
import icu.windea.pls.config.configGroup.extendedGameRules
import icu.windea.pls.config.configGroup.extendedInlineScripts
import icu.windea.pls.config.configGroup.extendedOnActions
import icu.windea.pls.config.configGroup.extendedParameters
import icu.windea.pls.config.configGroup.extendedScriptedVariables
import icu.windea.pls.core.icon
import icu.windea.pls.core.util.listOrEmpty
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.match.matchFromPattern
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

object ParadoxExtendedCompletionManager {
    fun completeExtendedScriptedVariable(context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val configGroup = context.configGroup ?: return
        configGroup.extendedScriptedVariables.values.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            val name = config0.name
            if (checkExtendedConfigName(name)) return@f
            val element = config0.pointer.element ?: return@f
            val typeFile = config0.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.ScriptedVariable)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withItemTextUnderlined(true) // used for completions from extended configs
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeExtendedDefinition(context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config ?: return
        val typeExpression = config.configExpression?.value ?: return
        val configGroup = config.configGroup
        val tailText = ParadoxCompletionManager.getExpressionTailText(context, config)
        run r1@{
            configGroup.extendedDefinitions.values.forEach { configs0 ->
                configs0.forEach f@{ config0 ->
                    ProgressManager.checkCanceled()
                    val name = config0.name
                    if (name.isEmpty()) return@f
                    if (checkExtendedConfigName(name)) return@f
                    val type = config0.type
                    if (!ParadoxDefinitionTypeExpression.resolve(type).matches(typeExpression)) return@f
                    val element = config0.pointer.element
                    val typeFile = config0.pointer.containingFile
                    val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                        .withTypeText(typeFile?.name, typeFile?.icon, true)
                        .withItemTextUnderlined(true) // used for completions from extended configs
                        .withPatchableIcon(PlsIcons.Nodes.Definition(type))
                        .withPatchableTailText(tailText)
                        .forScriptExpression(context)
                    result.addElement(lookupElement, context)
                }
            }
        }
        run r1@{
            val tGameRule = ParadoxDefinitionTypes.GameRule
            if (typeExpression != tGameRule) return@r1
            configGroup.extendedGameRules.values.forEach f@{ config0 ->
                ProgressManager.checkCanceled()
                val name = config0.name
                if (checkExtendedConfigName(name)) return@f
                val element = config0.pointer.element
                val typeFile = config0.pointer.containingFile
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withItemTextUnderlined(true) // used for completions from extended configs
                    .withPatchableIcon(PlsIcons.Nodes.Definition(tGameRule))
                    .withPatchableTailText(tailText)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
            }
        }
        run r1@{
            val tOnAction = ParadoxDefinitionTypes.OnAction
            if (typeExpression != tOnAction) return@r1
            configGroup.extendedOnActions.values.forEach f@{ config0 ->
                ProgressManager.checkCanceled()
                val name = config0.name
                if (checkExtendedConfigName(name)) return@f
                val element = config0.pointer.element
                val typeFile = config0.pointer.containingFile
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withItemTextUnderlined(true) // used for completions from extended configs
                    .withPatchableIcon(PlsIcons.Nodes.Definition(tOnAction))
                    .withPatchableTailText(tailText)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
            }
        }
    }

    fun completeExtendedInlineScript(context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config ?: return
        val configGroup = config.configGroup
        val tailText = ParadoxCompletionManager.getExpressionTailText(context, config)
        configGroup.extendedInlineScripts.values.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            val name = config0.name
            if (checkExtendedConfigName(name)) return@f
            val element = config0.pointer.element
            val typeFile = config0.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withItemTextUnderlined(true) // used for completions from extended configs
                .withPatchableIcon(PlsIcons.Nodes.InlineScript)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeExtendedParameter(context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val configGroup = context.configGroup ?: return
        val contextKey = context.contextKey ?: return
        val argumentNames = context.argumentNames
        val contextElement = context.contextElement ?: return
        configGroup.extendedParameters.values.forEach { configs0 ->
            configs0.forEach f@{ config0 ->
                if (!config0.contextKey.matchFromPattern(contextKey, contextElement, configGroup)) return@f
                val name = config0.name
                if (checkExtendedConfigName(name)) return@f
                if (argumentNames != null && !argumentNames.add(name)) return@f  // 排除已输入的
                val element = config0.pointer.element
                val typeFile = config0.pointer.containingFile
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withItemTextUnderlined(true) // used for completions from extended configs
                    .withPatchableIcon(PlsIcons.Nodes.Parameter)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
            }
        }
    }

    fun completeExtendedComplexEnumValue(context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config ?: return
        val enumName = config.configExpression?.value ?: return
        val configGroup = config.configGroup
        val tailText = ParadoxCompletionManager.getExpressionTailText(context, config)
        configGroup.extendedComplexEnumValues[enumName]?.values?.forEach f@{ config0 ->
            ProgressManager.checkCanceled()
            val name = config0.name
            if (checkExtendedConfigName(name)) return@f
            val element = config0.pointer.element
            val typeFile = config0.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withItemTextUnderlined(true) // used for completions from extended configs
                .withPatchableIcon(PlsIcons.Nodes.EnumValue)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeExtendedDynamicValue(context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeByExtendedConfigs) return
        ProgressManager.checkCanceled()

        val config = context.config
        val configs = context.configs
        val finalConfigs = configs.ifEmpty { config.singleton.listOrEmpty() }
        if (finalConfigs.isEmpty()) return
        for (config in finalConfigs) {
            val dynamicValueType = config.configExpression?.value ?: continue
            val configGroup = config.configGroup
            val tailText = ParadoxCompletionManager.getExpressionTailText(context, config)

            configGroup.extendedDynamicValues[dynamicValueType]?.values?.forEach f@{ config0 ->
                ProgressManager.checkCanceled()
                val name = config0.name
                if (checkExtendedConfigName(name)) return@f
                val type = config0.type
                val element = config0.pointer.element
                val typeFile = config0.pointer.containingFile
                val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withItemTextUnderlined(true) // used for completions from extended configs
                    .withPatchableIcon(PlsIcons.Nodes.DynamicValue(type))
                    .withPatchableTailText(tailText)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
            }
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
