package icu.windea.pls.lang.codeInsight.completion

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.*
import com.intellij.icons.*
import com.intellij.openapi.command.*
import com.intellij.openapi.command.impl.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.codeStyle.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*

object CwtConfigCompletionManager {
    object Keys : KeyRegistry()

    //region Accessors

    var ProcessingContext.expressionElement: CwtExpressionElement? by createKey(Keys)
    var ProcessingContext.containerElement: PsiElement? by createKey(Keys)
    var ProcessingContext.keyToMatch: String? by createKey(Keys)
    var ProcessingContext.optionContainerIdToMatch: String? by createKey(Keys)
    var ProcessingContext.schema: CwtSchemaConfig? by createKey(Keys)
    var ProcessingContext.contextConfigs: List<CwtMemberConfig<*>> by createKey(Keys) { emptyList() }
    var ProcessingContext.isOptionKey: Boolean by createKey(Keys) { false }
    var ProcessingContext.isOptionValue: Boolean by createKey(Keys) { false }
    var ProcessingContext.isOptionBlockValue: Boolean by createKey(Keys) { false }
    var ProcessingContext.inOption: Boolean by createKey(Keys) { false }
    var ProcessingContext.isPropertyKey: Boolean by createKey(Keys) { false }
    var ProcessingContext.isPropertyValue: Boolean by createKey(Keys) { false }
    var ProcessingContext.isBlockValue: Boolean by createKey(Keys) { false }
    var ProcessingContext.isKey: Boolean by createKey(Keys) { false }
    var ProcessingContext.isKeyOnly: Boolean by createKey(Keys) { false }
    var ProcessingContext.isValueOnly: Boolean by createKey(Keys) { false }

    //endregion

    //region Predefined Lookup Elements

    val yesLookupElement = LookupElementBuilder.create("yes").bold()
        .withPriority(CwtConfigCompletionPriorities.keyword)
        .withCompletionId()

    val noLookupElement = LookupElementBuilder.create("no").bold()
        .withPriority(CwtConfigCompletionPriorities.keyword)
        .withCompletionId()

    val blockLookupElement = LookupElementBuilder.create("")
        .withPresentableText("{...}")
        .withInsertHandler { c, _ ->
            val editor = c.editor
            val customSettings = CodeStyle.getCustomSettings(c.file, CwtCodeStyleSettings::class.java)
            val spaceWithinBraces = customSettings.SPACE_WITHIN_BRACES
            val text = if (spaceWithinBraces) "{  }" else "{}"
            val length = if (spaceWithinBraces) text.length - 2 else text.length - 1
            EditorModificationUtil.insertStringAtCaret(editor, text, false, true, length)
        }
        .withPriority(CwtConfigCompletionPriorities.keyword)
        .withCompletionId()

    //endregion

    //region Core Methods

    fun initializeContext(contextElement: PsiElement, parameters: CompletionParameters, context: ProcessingContext): Boolean {
        val configGroup = CwtConfigManager.getContainingConfigGroup(parameters.originalFile) ?: return false
        context.configGroup = configGroup

        context.parameters = parameters
        context.completionIds = mutableSetOf<String>().synced()

        val quoted = contextElement.text.isLeftQuoted()
        val rightQuoted = contextElement.text.isRightQuoted()
        val offsetInParent = parameters.offset - contextElement.startOffset
        val keyword = contextElement.getKeyword(offsetInParent)

        context.contextElement = contextElement
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.quoted = quoted
        context.rightQuoted = rightQuoted

        return true
    }

    fun initializeContextForConfigCompletions(context: ProcessingContext): Boolean {
        val contextElement = context.contextElement ?: return false
        val configGroup = context.configGroup ?: return false

        val expressionElement = getExpressionElement(contextElement) ?: return false
        val containerElement = getContainerElement(expressionElement) ?: return false
        val keyToMatch = getKeyToMatch(contextElement)
        val optionContainerIdToMatch = getOptionContainerIdToMatch(expressionElement)

        val schema = configGroup.schemas.firstOrNull() ?: return false
        val contextConfigs = CwtConfigManager.getContextConfigs(expressionElement, containerElement, schema)

        val isOptionKey = contextElement is CwtOptionKey || (contextElement is CwtString && contextElement.isOptionBlockValue())
        val isOptionBlockValue = contextElement is CwtString && contextElement.isOptionBlockValue()
        val isOptionValue = contextElement is CwtString && contextElement.isOptionValue()
        val inOption = isOptionKey || isOptionBlockValue || isOptionValue

        val isPropertyKey = expressionElement is CwtPropertyKey || expressionElement is CwtString && expressionElement.isBlockValue()
        val isBlockValue = expressionElement is CwtString && expressionElement.isBlockValue()
        val isPropertyValue = expressionElement is CwtString && expressionElement.isPropertyValue()

        val isKey = if (inOption) isOptionKey else isPropertyKey
        val isKeyOnly = contextElement is CwtOptionKey || contextElement is CwtPropertyKey
        val isValueOnly = contextElement is CwtString && if (inOption) !isOptionKey else !isPropertyKey

        context.expressionElement = expressionElement
        context.containerElement = containerElement
        context.keyToMatch = keyToMatch
        context.optionContainerIdToMatch = optionContainerIdToMatch
        context.schema = schema
        context.contextConfigs = contextConfigs
        context.isOptionKey = isOptionKey
        context.isOptionValue = isOptionValue
        context.isOptionBlockValue = isOptionBlockValue
        context.inOption = inOption
        context.isPropertyKey = isPropertyKey
        context.isPropertyValue = isPropertyValue
        context.isBlockValue = isBlockValue
        context.isKey = isKey
        context.isKeyOnly = isKeyOnly
        context.isValueOnly = isValueOnly

        return true
    }

    private fun getExpressionElement(element: PsiElement): CwtExpressionElement? {
        if (element is CwtOptionKey || (element is CwtString && (element.isOptionValue() || element.isOptionBlockValue()))) {
            val parentElementType = element.parent.elementType ?: return null
            if (parentElementType != CwtElementTypes.OPTION_COMMENT_TOKEN && parentElementType != CwtElementTypes.OPTION) return null
            val memberElement = element.parentOfType<CwtOptionComment>()?.siblings(withSelf = false)?.findIsInstance<CwtMemberElement>() ?: return null
            return when (memberElement) {
                is CwtProperty -> memberElement.propertyKey
                is CwtValue -> memberElement
                else -> null
            }
        }
        return element.castOrNull()
    }

    private fun getContainerElement(expressionElement: PsiElement): PsiElement? {
        val result = when {
            expressionElement is CwtPropertyKey -> expressionElement.parent?.parent
            expressionElement is CwtString -> expressionElement.parent
            else -> null
        }
        if (result !is CwtProperty && result !is CwtBlockElement) return null
        return result
    }

    private fun getKeyToMatch(element: PsiElement): String? {
        if (element !is CwtString) return null
        val keyElement = element.parent?.firstChild?.takeIf { it is CwtOptionKey || it is CwtPropertyKey } ?: return null
        return keyElement.text.unquote()
    }

    private fun getOptionContainerIdToMatch(expressionElement: PsiElement): String? {
        return when {
            expressionElement is CwtPropertyKey -> "#" + expressionElement.value
            expressionElement is CwtValue -> expressionElement.value
            else -> null
        }
    }

    fun addConfigCompletions(context: ProcessingContext, result: CompletionResultSet) {
        val schema = context.schema!!
        val contextConfigs = context.contextConfigs
        if (contextConfigs.isEmpty()) {
            return completeByDeclarationConfig(schema, context, result)
        }
        completeByContextConfigs(contextConfigs, schema, context, result)
    }

    private fun completeByDeclarationConfig(schema: CwtSchemaConfig, context: ProcessingContext, result: CompletionResultSet) {
        val declarationConfig = schema.constraints["declaration"] ?: return
        if (context.inOption) {
            return completeByOptionConfigs(declarationConfig, schema, context, result)
        }
    }

    private fun completeByContextConfigs(contextConfigs: List<CwtMemberConfig<*>>, schema: CwtSchemaConfig, context: ProcessingContext, result: CompletionResultSet) {
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
            configs.find { it is CwtPropertyConfig && it.valueType != CwtType.Block }?.also { filteredConfigs += it }
            configs.find { it is CwtPropertyConfig && it.valueType == CwtType.Block }?.also { filteredConfigs += it }
            filteredConfigs.forEach f@{ config ->
                if (context.inOption) {
                    //这个过滤条件并不是十分准确，未来可以考虑进一步优化
                    if (context.optionContainerIdToMatch != id && !id.contains('$')) return@f
                    completeByOptionConfigs(config, schema, context, result)
                } else {
                    completeByConfig(schema, config, context, result)
                }
            }
        }
    }

    private fun completeByConfig(schema: CwtSchemaConfig, config: CwtMemberConfig<*>, context: ProcessingContext, result: CompletionResultSet) {
        when (config) {
            is CwtPropertyConfig -> {
                if (context.isPropertyKey) {
                    val schemaExpression = CwtSchemaExpression.resolve(config.key)
                    completeBySchemaExpression(schemaExpression, schema, config, context, result)
                } else if (context.isPropertyValue) {
                    //这个过滤条件并不是十分准确，未来可以考虑进一步优化
                    if (context.keyToMatch != config.key && !config.key.contains('$')) return
                    if (config.valueType != CwtType.Block) {
                        val schemaExpression = CwtSchemaExpression.resolve(config.value)
                        completeBySchemaExpression(schemaExpression, schema, config, context, result)
                    } else {
                        result.addElement(blockLookupElement, context)
                    }
                }
            }
            is CwtValueConfig -> {
                if (context.isBlockValue) {
                    if (config.valueType != CwtType.Block) {
                        val schemaExpression = CwtSchemaExpression.resolve(config.value)
                        completeBySchemaExpression(schemaExpression, schema, config, context, result)
                    } else {
                        result.addElement(blockLookupElement, context)
                    }
                }
            }
        }
    }

    private fun completeByOptionConfigs(config: CwtMemberConfig<*>, schema: CwtSchemaConfig, context: ProcessingContext, result: CompletionResultSet) {
        val optionConfigs = config.optionConfigs
        if (optionConfigs.isNullOrEmpty()) return
        val optionConfigsGroup = optionConfigs.groupBy { optionConfig ->
            when (optionConfig) {
                is CwtOptionConfig -> "#" + optionConfig.key
                is CwtOptionValueConfig -> optionConfig.value
            }
        }
        optionConfigsGroup.forEach { (_, configs) ->
            val filteredConfigs = mutableListOf<CwtOptionMemberConfig<*>>()
            configs.find { it is CwtOptionValueConfig }?.also { filteredConfigs += it }
            configs.find { it is CwtOptionConfig && it.valueType != CwtType.Block }?.also { filteredConfigs += it }
            configs.find { it is CwtOptionConfig && it.valueType == CwtType.Block }?.also { filteredConfigs += it }
            filteredConfigs.forEach { config ->
                completeByOptionConfig(schema, config, context, result)
            }
        }
    }

    private fun completeByOptionConfig(schema: CwtSchemaConfig, config: CwtOptionMemberConfig<*>, context: ProcessingContext, result: CompletionResultSet) {
        when (config) {
            is CwtOptionConfig -> {
                if (context.isOptionKey) {
                    val schemaExpression = CwtSchemaExpression.resolve(config.key)
                    completeBySchemaExpression(schemaExpression, schema, config, context, result)
                } else if (context.isOptionValue) {
                    //这个过滤条件并不是十分准确，未来可以考虑进一步优化
                    if (context.keyToMatch != config.key && !config.key.contains('$')) return
                    if (config.valueType != CwtType.Block) {
                        val schemaExpression = CwtSchemaExpression.resolve(config.value)
                        completeBySchemaExpression(schemaExpression, schema, config, context, result)
                    } else {
                        result.addElement(blockLookupElement, context)
                    }
                }
            }
            is CwtOptionValueConfig -> {
                if (context.isOptionBlockValue) {
                    if (config.valueType != CwtType.Block) {
                        val schemaExpression = CwtSchemaExpression.resolve(config.value)
                        completeBySchemaExpression(schemaExpression, schema, config, context, result)
                    } else {
                        result.addElement(blockLookupElement, context)
                    }
                }
            }
        }
    }

    fun completeBySchemaExpression(
        schemaExpression: CwtSchemaExpression,
        schema: CwtSchemaConfig,
        config: CwtConfig<*>,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        completeBySchemaExpression(schemaExpression, schema, config) {
            val lookupElement = it.forConfig(context, config, schemaExpression)
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeBySchemaExpression(
        schemaExpression: CwtSchemaExpression,
        schema: CwtSchemaConfig,
        config: CwtConfig<*>,
        processor: Processor<LookupElementBuilder>
    ): Boolean {
        val icon = when {
            schemaExpression is CwtSchemaExpression.Enum -> AllIcons.Nodes.Enum
            config is CwtOptionConfig -> PlsIcons.Nodes.CwtOption
            config is CwtOptionValueConfig -> PlsIcons.Nodes.CwtValue
            config is CwtPropertyConfig -> PlsIcons.Nodes.CwtProperty
            config is CwtValueConfig -> PlsIcons.Nodes.CwtValue
            else -> null
        }
        val typeFile = schema.file.pointer.element
        return when (schemaExpression) {
            is CwtSchemaExpression.Constant -> {
                val element = config.pointer.element
                val v = schemaExpression.expressionString
                val lookupElement = LookupElementBuilder.create(v).withPsiElement(element)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withIcon(icon)
                    .withPriority(CwtConfigCompletionPriorities.constant)
                processor.process(lookupElement)
            }
            is CwtSchemaExpression.Enum -> {
                val enumName = schemaExpression.name
                val tailText = " by ${schemaExpression}"
                val enumValueConfigs = schema.enums[enumName]?.values ?: return true
                enumValueConfigs.process p@{
                    val element = it.pointer.element
                    val v = it.stringValue ?: return@p true
                    val lookupElement = LookupElementBuilder.create(v).withPsiElement(element)
                        .withTypeText(typeFile?.name, typeFile?.icon, true)
                        .withIcon(icon)
                        .withPriority(CwtConfigCompletionPriorities.enumValue)
                        .withPatchableTailText(tailText)
                    processor.process(lookupElement)
                }
            }
            is CwtSchemaExpression.Template -> {
                val v = schemaExpression.expressionString
                val element = config.pointer.element
                val tailText = " (template)"
                val lookupElement = LookupElementBuilder.create(v).withPsiElement(element)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withIcon(icon)
                    .withPatchableTailText(tailText)
                processor.process(lookupElement)
            }
            is CwtSchemaExpression.Type -> {
                val typeName = schemaExpression.name
                if (typeName == "bool" || typeName == "any") {
                    processor.process(yesLookupElement)
                    processor.process(noLookupElement)
                }
                if (typeName == "any") {
                    processor.process(blockLookupElement)
                }
                //TODO 1.3.19+
                true
            }
            is CwtSchemaExpression.Constraint -> true
        }
    }

    fun completeByTemplateExpression(
        templateExpression: CwtConfigTemplateExpression,
        context: ExpressionContext,
        processor: Processor<LookupElementBuilder>
    ): Boolean {
        val icon = when {
            templateExpression is CwtConfigTemplateExpression.Enum -> AllIcons.Nodes.Enum
            templateExpression is CwtConfigTemplateExpression.Parameter -> AllIcons.Nodes.Parameter
            else -> null
        }

        val configGroup = templateExpression.context.configGroup ?: return true
        val schema = configGroup.schemas.firstOrNull() ?: return true

        val tailText = " by ${templateExpression.text}"
        return when (templateExpression) {
            is CwtConfigTemplateExpression.Enum -> {
                fun processLookupElement(config: CwtValueConfig? = null): Boolean {
                    if (config == null) return true
                    val v = config.stringValue ?: return true
                    val element = config.pointer.element
                    val typeFile = config.pointer.containingFile
                    val lookupElement = LookupElementBuilder.create(v).withPsiElement(element)
                        .withIcon(icon)
                        .withTailText(tailText, true)
                        .withTypeText(typeFile?.name, typeFile?.icon, true)
                    return processor.process(lookupElement)
                }

                val enumName = templateExpression.name
                val finalConfigs = schema.enums[enumName]?.values ?: return true
                finalConfigs.process { processLookupElement(it) }
            }
            is CwtConfigTemplateExpression.Parameter -> {
                fun processLookupElement(name: String, config: CwtConfig<*>? = null): Boolean {
                    if (config == null) return true
                    val element = config.pointer.element
                    val lookupElement = LookupElementBuilder.create(name).withPsiElement(element)
                        .withIcon(icon)
                        .withTailText(tailText, true)
                    return processor.process(lookupElement)
                }

                //currently only calculate from configs
                when (templateExpression.name) {
                    "system_scope" -> {
                        val finalConfigs = configGroup.systemScopes
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "localisation_locale" -> {
                        val finalConfigs = configGroup.localisationLocalesById
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "type" -> {
                        val finalConfigs = configGroup.types
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "subtype" -> {
                        val contextElement = templateExpression.context.contextElement ?: return true
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
                        val contextElement = templateExpression.context.contextElement ?: return true
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
                        val contextElement = templateExpression.context.contextElement ?: return true
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
                        true //no completion yet
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
                    "inline_script" -> {
                        val finalConfigs = configGroup.extendedInlineScripts
                        finalConfigs.process { (n, c) -> processLookupElement(n, c) }
                    }
                    "parameter" -> {
                        val finalConfigs = configGroup.extendedParameters
                        finalConfigs.process { (n, c) -> processLookupElement(n, c.singleOrNull()) }
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
                    "inline" -> {
                        val finalConfigs = configGroup.inlineConfigGroup
                        finalConfigs.process { (n, c) -> processLookupElement(n, c.singleOrNull()) }
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

    private fun LookupElementBuilder.forConfig(context: ProcessingContext, config: CwtConfig<*>, schemaExpression: CwtSchemaExpression): LookupElement? {
        var lookupElement = this

        if (lookupElement == yesLookupElement || lookupElement == noLookupElement || lookupElement == blockLookupElement) return lookupElement

        val isKeyConfig = config is CwtOptionConfig || config is CwtPropertyConfig
        val insertCurlyBraces = when {
            config is CwtOptionMemberConfig<*> -> config.valueType == CwtType.Block
            config is CwtMemberConfig<*> -> config.valueType == CwtType.Block
            else -> return null
        }
        val valueText = when {
            insertCurlyBraces -> PlsStringConstants.blockFolder
            config is CwtOptionMemberConfig<*> -> config.value
            config is CwtMemberConfig<*> -> config.value
            else -> return null
        }

        val patchableTailText = this.patchableTailText
        val tailText = buildString {
            if (isKeyConfig && !context.isKeyOnly && !context.isValueOnly) append(" = ").append(valueText)
            if (patchableTailText != null) append(patchableTailText)
        }
        lookupElement = lookupElement.withTailText(tailText, true)

        if (context.isKeyOnly || context.isValueOnly) { //key or value only
            lookupElement = lookupElement.withInsertHandler { c, _ -> applyKeyOrValueInsertHandler(c, context) }
        }
        if (isKeyConfig && context.isKey && !context.isKeyOnly) { // key with value
            lookupElement = lookupElement.withInsertHandler { c, _ -> applyKeyWithValueInsertHandler(c, context, insertCurlyBraces) }
        }

        if (schemaExpression is CwtSchemaExpression.Template) {
            val insertHandler0 = lookupElement.insertHandler
            lookupElement = lookupElement.withInsertHandler { c, item ->
                val caretOffset1 = c.editor.caretModel.offset
                insertHandler0?.handleInsert(c, item)
                val caretOffset2 = c.editor.caretModel.offset
                val caretMarker = c.editor.document.createRangeMarker(caretOffset1, caretOffset2)
                caretMarker.isGreedyToRight = true
                c.editor.caretModel.moveToOffset(caretMarker.startOffset)
                applyExpandTemplateInsertHandler(c, context, schemaExpression, caretMarker)
            }
        }

        return lookupElement
    }

    private fun applyKeyOrValueInsertHandler(c: InsertionContext, context: ProcessingContext) {
        //这里的isKey需要在创建LookupElement时就预先获取（之后可能会有所变更）
        //这里的isKey如果是null，表示已经填充的只是KEY或VALUE的其中一部分
        if (!context.quoted) return
        val editor = c.editor
        val caretOffset = editor.caretModel.offset
        val charsSequence = editor.document.charsSequence
        val rightQuoted = charsSequence.get(caretOffset) == '"' && charsSequence.get(caretOffset - 1) != '\\'
        if (rightQuoted) {
            //在必要时将光标移到右双引号之后
            editor.caretModel.moveToOffset(caretOffset + 1)
        } else {
            //插入缺失的右双引号，且在必要时将光标移到右双引号之后
            EditorModificationUtil.insertStringAtCaret(editor, "\"", false, true)
        }
    }

    private fun applyKeyWithValueInsertHandler(c: InsertionContext, context: ProcessingContext, insertCurlyBraces: Boolean) {
        val editor = c.editor
        applyKeyOrValueInsertHandler(c, context)
        val customSettings = CodeStyle.getCustomSettings(c.file, CwtCodeStyleSettings::class.java)
        val spaceAroundPropertySeparator = customSettings.SPACE_AROUND_PROPERTY_SEPARATOR
        val spaceWithinBraces = customSettings.SPACE_WITHIN_BRACES
        val text = buildString {
            if (spaceAroundPropertySeparator) append(" ")
            append("=")
            if (spaceAroundPropertySeparator) append(" ")
            if (insertCurlyBraces) {
                if (spaceWithinBraces) append("{  }") else append("{}")
            }
        }
        val length = if (insertCurlyBraces) {
            if (spaceWithinBraces) text.length - 2 else text.length - 1
        } else {
            text.length
        }
        EditorModificationUtil.insertStringAtCaret(editor, text, false, true, length)
    }

    private fun applyExpandTemplateInsertHandler(c: InsertionContext, context: ProcessingContext, schemaExpression: CwtSchemaExpression.Template, caretMarker: RangeMarker) {
        val file = context.parameters?.originalFile ?: return
        c.laterRunnable = Runnable {
            val project = file.project
            val editor = c.editor
            val documentManager = PsiDocumentManager.getInstance(project)
            val command = Runnable c@{
                documentManager.commitDocument(editor.document)
                val elementOffset = caretMarker.startOffset - 1
                val element = file.findElementAt(elementOffset)?.parent
                if (element !is CwtPropertyKey && element !is CwtString) return@c
                val startAction = StartMarkAction.start(editor, project, PlsBundle.message("config.command.expandTemplate.name"))
                val templateBuilder = TemplateBuilderFactory.getInstance().createTemplateBuilder(element)
                val shift = element.startOffset + if (context.quoted) 1 else 0
                schemaExpression.parameterRanges.forEach { parameterRange ->
                    val parameterText = parameterRange.substring(schemaExpression.expressionString)
                    val expression = CwtConfigTemplateExpression.resolve(context, schemaExpression, parameterRange, parameterText)
                        ?: TextExpression(parameterText)
                    templateBuilder.replaceRange(parameterRange.shiftRight(shift), expression)
                }
                val textRange = element.textRange
                editor.caretModel.moveToOffset(textRange.startOffset)
                val template = templateBuilder.buildInlineTemplate()
                TemplateManager.getInstance(project).startTemplate(editor, template, TemplateEditingFinishedListener { _, _ ->
                    c.editor.caretModel.moveToOffset(caretMarker.endOffset)
                    FinishMarkAction.finish(project, editor, startAction)
                })
            }
            WriteCommandAction.runWriteCommandAction(project, PlsBundle.message("config.command.expandTemplate.name"), null, command, file)
        }
    }

    //endregion
}
