package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.icons.AllIcons
import com.intellij.util.Processor
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configExpression.CwtSchemaExpression
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.model.paths.CwtConfigPath
import icu.windea.pls.model.type.CwtExpressionType

object CwtCompletionManager {
    // region Entry Completion Extensions

    fun addConfigCompletions(context: CwtCompletionContext, result: CompletionResultSet) {
        val schema = context.schema!!
        val contextConfigs = context.contextConfigs
        if (contextConfigs.isEmpty()) {
            return completeByDeclarationConfig(context, result, schema)
        }
        completeByContextConfigs(context, result, schema, contextConfigs)
    }

    // endregion

    // region General Completion Extensions

    private fun completeByDeclarationConfig(context: CwtCompletionContext, result: CompletionResultSet, schema: CwtSchemaConfig) {
        val declarationConfig = schema.constraints["declaration"] ?: return
        if (context.inOption) {
            return completeByOptionConfigs(context, result, schema, declarationConfig)
        }
    }

    private fun completeByContextConfigs(context: CwtCompletionContext, result: CompletionResultSet, schema: CwtSchemaConfig, contextConfigs: List<CwtMemberConfig<*>>) {
        if (contextConfigs.isEmpty()) return
        val contextConfigsGroup = contextConfigs.groupBy { config ->
            when (config) {
                is CwtPropertyConfig -> "#" + config.key
                is CwtValueConfig -> config.value
            }
        }
        contextConfigsGroup.forEach { (id, configs) ->
            val filteredConfigs = mutableListOf<CwtMemberConfig<*>>()
            configs.find { it is CwtValueConfig }?.also { filteredConfigs += it }
            configs.find { it is CwtPropertyConfig && it.valueType != CwtExpressionType.Block }?.also { filteredConfigs += it }
            configs.find { it is CwtPropertyConfig && it.valueType == CwtExpressionType.Block }?.also { filteredConfigs += it }
            filteredConfigs.forEach f@{ config ->
                if (context.inOption) {
                    // 这个过滤条件并不是十分准确，未来可以考虑进一步优化
                    if (context.optionContainerIdToMatch != id && !id.contains('$')) return@f
                    completeByOptionConfigs(context, result, schema, config)
                } else {
                    completeByConfig(context, result, schema, config)
                }
            }
        }
    }

    private fun completeByConfig(context: CwtCompletionContext, result: CompletionResultSet, schema: CwtSchemaConfig, config: CwtMemberConfig<*>) {
        when (config) {
            is CwtPropertyConfig -> {
                if (context.isPropertyKey) {
                    val schemaExpression = CwtSchemaExpression.resolve(config.key)
                    completeBySchemaExpression(context, result, schema, config, schemaExpression)
                } else if (context.isPropertyValue) {
                    // 这个过滤条件并不是十分准确，未来可以考虑进一步优化
                    if (context.keyToMatch != config.key && !config.key.contains('$')) return
                    if (config.valueType != CwtExpressionType.Block) {
                        val schemaExpression = CwtSchemaExpression.resolve(config.value)
                        completeBySchemaExpression(context, result, schema, config, schemaExpression)
                    } else {
                        CwtCompletionFactory.forBlockKeyword().addToResult(context, result)
                    }
                }
            }
            is CwtValueConfig -> {
                if (context.isDirectValue) {
                    if (config.valueType != CwtExpressionType.Block) {
                        val schemaExpression = CwtSchemaExpression.resolve(config.value)
                        completeBySchemaExpression(context, result, schema, config, schemaExpression)
                    } else {
                        CwtCompletionFactory.forBlockKeyword().addToResult(context, result)
                    }
                }
            }
        }
    }

    private fun completeByOptionConfigs(context: CwtCompletionContext, result: CompletionResultSet, schema: CwtSchemaConfig, config: CwtMemberConfig<*>) {
        val optionConfigs = config.optionMetadata.optionConfigs
        if (optionConfigs.isEmpty()) return
        val optionConfigsGroup = optionConfigs.groupBy { optionConfig ->
            when (optionConfig) {
                is CwtOptionConfig -> "#" + optionConfig.key
                is CwtOptionValueConfig -> optionConfig.value
            }
        }
        optionConfigsGroup.forEach { (_, configs) ->
            val filteredConfigs = mutableListOf<CwtOptionMemberConfig<*>>()
            configs.find { it is CwtOptionValueConfig }?.also { filteredConfigs += it }
            configs.find { it is CwtOptionConfig && it.valueType != CwtExpressionType.Block }?.also { filteredConfigs += it }
            configs.find { it is CwtOptionConfig && it.valueType == CwtExpressionType.Block }?.also { filteredConfigs += it }
            filteredConfigs.forEach { config ->
                completeByOptionConfig(context, result, schema, config)
            }
        }
    }

    private fun completeByOptionConfig(context: CwtCompletionContext, result: CompletionResultSet, schema: CwtSchemaConfig, config: CwtOptionMemberConfig<*>) {
        when (config) {
            is CwtOptionConfig -> {
                if (context.isOptionKey) {
                    val schemaExpression = CwtSchemaExpression.resolve(config.key)
                    completeBySchemaExpression(context, result, schema, config, schemaExpression)
                } else if (context.isOptionValue) {
                    // 这个过滤条件并不是十分准确，未来可以考虑进一步优化
                    if (context.keyToMatch != config.key && !config.key.contains('$')) return
                    if (config.valueType != CwtExpressionType.Block) {
                        val schemaExpression = CwtSchemaExpression.resolve(config.value)
                        completeBySchemaExpression(context, result, schema, config, schemaExpression)
                    } else {
                        CwtCompletionFactory.forBlockKeyword().addToResult(context, result)
                    }
                }
            }
            is CwtOptionValueConfig -> {
                if (context.isOptionDirectValue) {
                    if (config.valueType != CwtExpressionType.Block) {
                        val schemaExpression = CwtSchemaExpression.resolve(config.value)
                        completeBySchemaExpression(context, result, schema, config, schemaExpression)
                    } else {
                        CwtCompletionFactory.forBlockKeyword().addToResult(context, result)
                    }
                }
            }
        }
    }

    private fun completeBySchemaExpression(context: CwtCompletionContext, result: CompletionResultSet, schema: CwtSchemaConfig, config: CwtConfig<*>, schemaExpression: CwtSchemaExpression) {
        completeFromSchemaExpression(schema, config, schemaExpression) {
            it.wrapForConfig(context, config, schemaExpression).addToResult(context, result)
        }
    }

    fun completeFromSchemaExpression(schema: CwtSchemaConfig, config: CwtConfig<*>, schemaExpression: CwtSchemaExpression, processor: Processor<LookupElementBuilder>): Boolean {
        val icon = when {
            schemaExpression is CwtSchemaExpression.Enum -> AllIcons.Nodes.Enum
            config is CwtOptionConfig -> ChronicleIcons.Nodes.Option
            config is CwtOptionValueConfig -> ChronicleIcons.Nodes.Value
            config is CwtPropertyConfig -> ChronicleIcons.Nodes.Property
            config is CwtValueConfig -> ChronicleIcons.Nodes.Value
            else -> null
        }
        val typeFile = schema.file.pointer.element
        return when (schemaExpression) {
            is CwtSchemaExpression.Constant -> {
                val lookupString = schemaExpression.expressionString
                val element = config.pointer.element
                val lookupElement = CwtCompletionFactory.forSchemaConstant(lookupString, element, typeFile, icon)
                processor.process(lookupElement)
            }
            is CwtSchemaExpression.Enum -> {
                val hintText = " by ${schemaExpression}"

                fun processLookupElement(config: CwtValueConfig? = null): Boolean {
                    if (config == null) return true
                    val lookupString = config.stringValue ?: return true
                    val element = config.pointer.element
                    val lookupElement = CwtCompletionFactory.forSchemaEnumValue(lookupString, element, typeFile, icon, hintText)
                    return processor.process(lookupElement)
                }

                val enumName = schemaExpression.name
                val enumValueConfigs = schema.enums[enumName]?.values ?: return true
                enumValueConfigs.process { processLookupElement(it) }
            }
            is CwtSchemaExpression.Template -> {
                val lookupString = schemaExpression.expressionString
                val element = config.pointer.element
                val hintText = " (template)"
                val lookupElement = CwtCompletionFactory.forSchemaTemplate(lookupString, element, typeFile, icon, hintText)
                processor.process(lookupElement)
            }
            is CwtSchemaExpression.Type -> {
                when (schemaExpression.name) {
                    "any" -> CwtCompletionFactory.forKeyword().forEach { processor.process(it) }
                    "bool" -> CwtCompletionFactory.forBool().forEach { processor.process(it) }
                    "cardinality" -> CwtCompletionFactory.forCardinality().forEach { processor.process(it) }
                }
                true
            }
            is CwtSchemaExpression.Constraint -> true
            else -> true
        }
    }

    fun completeFromTemplateExpression(templateExpression: CwtCompletionTemplateExpression, context: ExpressionContext, processor: Processor<LookupElementBuilder>): Boolean {
        val configGroup = templateExpression.context.configGroup
        val schema = templateExpression.context.schema ?: return true
        val hintText = " by ${templateExpression.text}"
        return when (templateExpression) {
            is CwtCompletionTemplateExpression.Enum -> {
                fun processLookupElement(config: CwtValueConfig? = null): Boolean {
                    if (config == null) return true
                    val lookupString = config.stringValue ?: return true
                    val element = config.pointer.element
                    val typeFile = config.pointer.containingFile
                    val lookupElement = CwtCompletionFactory.forSchemaTemplateEnum(lookupString, element, typeFile, hintText)
                    return processor.process(lookupElement)
                }

                val enumName = templateExpression.name
                val finalConfigs = schema.enums[enumName]?.values ?: return true
                finalConfigs.process { processLookupElement(it) }
            }
            is CwtCompletionTemplateExpression.Parameter -> {
                fun processLookupElement(name: String, config: CwtConfig<*>? = null): Boolean {
                    if (config == null) return true
                    val lookupString = name
                    val element = config.pointer.element
                    val lookupElement = CwtCompletionFactory.forSchemaTemplateParameter(lookupString, element, hintText)
                    return processor.process(lookupElement)
                }

                // currently only calculate from configs
                when (templateExpression.name) {
                    "system_scope" -> {
                        val finalConfigs = configGroup.systemScopes
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "localisation_locale" -> {
                        val finalConfigs = configGroup.locales
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "type" -> {
                        val finalConfigs = configGroup.types
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "subtype" -> {
                        val contextElement = templateExpression.context.contextElement
                        val configPath = CwtConfigManager.getConfigPath(contextElement) ?: return true
                        val type = getTypeFromFromConfigPath(configPath) ?: return true
                        val finalConfigs = configGroup.types[type]?.subtypes ?: return true
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "enum" -> {
                        val finalConfigs = configGroup.enums
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "complex_enum" -> {
                        val finalConfigs = configGroup.complexEnums
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "complex_enum_value" -> {
                        val contextElement = templateExpression.context.contextElement
                        val configPath = CwtConfigManager.getConfigPath(contextElement) ?: return true
                        val complexEnum = getComplexEnumValueFromConfigPath(configPath) ?: return true
                        val finalConfigs = configGroup.extendedComplexEnumValues[complexEnum] ?: return true
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "dynamic_value_type" -> {
                        val finalConfigs = configGroup.dynamicValueTypes
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "dynamic_value" -> {
                        val contextElement = templateExpression.context.contextElement
                        val configPath = CwtConfigManager.getConfigPath(contextElement) ?: return true
                        val complexEnum = getDynamicValueFromConfigPath(configPath) ?: return true
                        val finalConfigs = configGroup.extendedDynamicValues[complexEnum]
                        finalConfigs?.process { (n, c) -> processLookupElement(n, c) } ?: true
                    }
                    "link" -> {
                        val finalConfigs = configGroup.links
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "scope" -> {
                        true // no completion yet
                    }
                    "localisation_link" -> {
                        val finalConfigs = configGroup.localisationLinks
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "localisation_command" -> {
                        val finalConfigs = configGroup.localisationCommands
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "modifier_category" -> {
                        val finalConfigs = configGroup.modifierCategories
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "modifier" -> {
                        val finalConfigs = configGroup.modifiers
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "scope_name" -> {
                        val finalConfigs = configGroup.scopes
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "scope_group" -> {
                        val finalConfigs = configGroup.scopeGroups
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "database_object_type" -> {
                        val finalConfigs = configGroup.databaseObjectTypes
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "scripted_variable" -> {
                        val finalConfigs = configGroup.extendedScriptedVariables
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "definition" -> {
                        val finalConfigs = configGroup.extendedDefinitions
                        finalConfigs.process { (n, c) -> processLookupElement(n, c.singleOrNull()) }
                    }
                    "game_rule" -> {
                        val finalConfigs = configGroup.extendedGameRules
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "on_action" -> {
                        val finalConfigs = configGroup.extendedOnActions
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "parameter" -> {
                        val finalConfigs = configGroup.extendedParameters
                        finalConfigs.process { (n, c) -> processLookupElement(n, c.singleOrNull()) }
                    }
                    "inline_script" -> {
                        val finalConfigs = configGroup.extendedInlineScripts
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "single_alias" -> {
                        val finalConfigs = configGroup.singleAliases
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "alias_name" -> {
                        val finalConfigs = configGroup.aliasGroups
                        finalConfigs.process { (n) -> processLookupElement(n) }
                    }
                    "alias_sub_name" -> {
                        val editor = context.editor ?: return true
                        val currentText = editor.document.charsSequence.substring(context.templateStartOffset, context.startOffset)
                        val aliasName = currentText.removeSurroundingOrNull("alias_name[", ":") ?: return true
                        val finalConfigs = configGroup.aliasGroups[aliasName] ?: return true
                        finalConfigs.process { (n, c) -> processLookupElement(n, c.singleOrNull()) }
                    }
                    "macro" -> {
                        true // no completion yet
                    }
                    else -> true
                }
            }
        }
    }

    private fun getTypeFromFromConfigPath(configPath: CwtConfigPath): String? {
        if (configPath.subPaths[0] != "types") return null
        return configPath.subPaths.getOrNull(1)?.removeSurroundingOrNull("type[", "]")
    }

    private fun getComplexEnumValueFromConfigPath(configPath: CwtConfigPath): String? {
        if (configPath.subPaths[0] != "complex_enum_values") return null
        return configPath.subPaths.getOrNull(1)
    }

    private fun getDynamicValueFromConfigPath(configPath: CwtConfigPath): String? {
        if (configPath.subPaths[0] != "dynamic_values") return null
        return configPath.subPaths.getOrNull(1)
    }

    // endregion
}
