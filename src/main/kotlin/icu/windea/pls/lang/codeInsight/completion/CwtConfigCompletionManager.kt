package icu.windea.pls.lang.codeInsight.completion

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.codeInsight.template.*
import com.intellij.icons.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.codeStyle.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

object CwtConfigCompletionManager {
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

    fun initializeContext(parameters: CompletionParameters, context: ProcessingContext, contextElement: PsiElement): Boolean {
        context.parameters = parameters
        context.completionIds = mutableSetOf<String>().synced()

        val configGroup = CwtConfigManager.getContainingConfigGroup(parameters.originalFile, forRepo = true) ?: return false
        context.configGroup = configGroup

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

    fun addConfigCompletions(context: ProcessingContext, result: CompletionResultSet) {
        val contextElement = context.contextElement ?: return //typing key / value
        val configGroup = context.configGroup ?: return

        val containerElement = when {
            contextElement is CwtPropertyKey -> contextElement.parent?.parent
            contextElement is CwtString && contextElement.isPropertyValue() -> contextElement.parent
            contextElement is CwtString/* && contextElement.isBlockValue()*/ -> contextElement.parent
            else -> null
        }
        if (containerElement !is CwtBlockElement && containerElement !is CwtProperty) return
        val schema = configGroup.schemas.firstOrNull() ?: return
        val contextConfigs = CwtConfigManager.getContextConfigs(contextElement, containerElement, schema)
        if (contextConfigs.isEmpty()) return

        val isKey = contextElement is CwtPropertyKey || contextElement is CwtString && contextElement.isBlockValue()
        val isBlockValue = contextElement is CwtString && contextElement.isBlockValue()
        val isPropertyValue = contextElement is CwtString && contextElement.isPropertyValue()

        val contextConfigsGroup = contextConfigs.groupBy { config ->
            when (config) {
                is CwtPropertyConfig -> config.key
                is CwtValueConfig -> config.value
            }
        }
        contextConfigsGroup.forEach f1@{ (_, configs) ->
            val filteredConfigs = mutableListOf<CwtMemberConfig<*>>()
            configs.find { it is CwtValueConfig }?.also { filteredConfigs += it }
            configs.find { it is CwtPropertyConfig && it.valueType != CwtType.Block }?.also { filteredConfigs += it }
            configs.find { it is CwtPropertyConfig && it.valueType == CwtType.Block }?.also { filteredConfigs += it }

            filteredConfigs.forEach f2@{ config ->
                val isBlock = config.valueType == CwtType.Block
                when (config) {
                    is CwtPropertyConfig -> {
                        if (isKey) {
                            val schemaExpression = CwtSchemaExpression.resolve(config.key)
                            completeBySchemaExpression(schemaExpression, schema, config) {
                                val lookupElement = it.forConfig(context, config, schemaExpression)
                                result.addElement(lookupElement, context)
                                true
                            }
                        } else if (isPropertyValue) {
                            if (isBlock) {
                                result.addElement(blockLookupElement, context)
                                return@f2
                            }
                            val schemaExpression = CwtSchemaExpression.resolve(config.value)
                            completeBySchemaExpression(schemaExpression, schema, config) {
                                val lookupElement = it.forConfig(context, config, schemaExpression)
                                result.addElement(lookupElement, context)
                                true
                            }
                        }
                    }
                    is CwtValueConfig -> {
                        if (isBlockValue) {
                            if (isBlock) {
                                result.addElement(blockLookupElement, context)
                                return@f2
                            }
                            val schemaExpression = CwtSchemaExpression.resolve(config.value)
                            completeBySchemaExpression(schemaExpression, schema, config) {
                                val lookupElement = it.forConfig(context, config, schemaExpression)
                                result.addElement(lookupElement, context)
                                true
                            }
                        }
                    }
                }
            }
        }
    }

    fun completeBySchemaExpression(
        schemaExpression: CwtSchemaExpression,
        schema: CwtSchemaConfig,
        config: CwtMemberConfig<*>,
        processor: Processor<LookupElementBuilder>
    ): Boolean {
        val icon = when {
            schemaExpression is CwtSchemaExpression.Enum -> AllIcons.Nodes.Enum
            config is CwtPropertyConfig -> PlsIcons.Nodes.Property
            config is CwtValueConfig -> PlsIcons.Nodes.Value
            else -> null
        }
        return when (schemaExpression) {
            is CwtSchemaExpression.Constant -> {
                val element = config.pointer
                val typeFile = element.containingFile
                val v = schemaExpression.expressionString
                val lookupElement = LookupElementBuilder.create(element, v)
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
                    val element = it.pointer
                    val typeFile = element.containingFile
                    val v = it.stringValue ?: return@p true
                    val lookupElement = LookupElementBuilder.create(element, v)
                        .withTypeText(typeFile?.name, typeFile?.icon, true)
                        .withIcon(icon)
                        .withPriority(CwtConfigCompletionPriorities.enumValue)
                        .withPatchableTailText(tailText)
                    processor.process(lookupElement)
                }
            }
            is CwtSchemaExpression.Template -> {
                val tailText = " (template)"
                val element = config.pointer
                val typeFile = element.containingFile
                val v = schemaExpression.expressionString
                val lookupElement = LookupElementBuilder.create(element, v)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withIcon(icon)
                    .withPatchableTailText(tailText)
                processor.process(lookupElement)
            }
            is CwtSchemaExpression.Type -> {
                val typeName = schemaExpression.name
                if (typeName == "bool" || typeName == "scalar" || typeName == "any") {
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
    ): Array<out LookupElement>? {
        val icon = when {
            templateExpression is CwtConfigTemplateExpression.Enum -> AllIcons.Nodes.Enum
            templateExpression is CwtConfigTemplateExpression.Parameter -> AllIcons.Nodes.Parameter
            else -> null
        }

        val configGroup = templateExpression.context.configGroup ?: return null
        val schema = configGroup.schemas.firstOrNull() ?: return null

        val tailText = " by ${templateExpression.text}"
        return when (templateExpression) {
            is CwtConfigTemplateExpression.Enum -> {
                val enumName = templateExpression.name
                val enumValueConfigs = schema.enums[enumName]?.values ?: return null
                enumValueConfigs.mapNotNull p@{
                    val element = it.pointer
                    val typeFile = element.containingFile
                    val v = it.stringValue ?: return@p null
                    LookupElementBuilder.create(element, v)
                        .withIcon(icon)
                        .withTailText(tailText, true)
                        .withTypeText(typeFile?.name, typeFile?.icon, true)
                }.toTypedArray()
            }
            is CwtConfigTemplateExpression.Parameter -> {
                fun createLookupItem(name: String, config: CwtConfig<*>? = null): LookupElement {
                    return LookupElementBuilder.create(name).withPsiElement(config?.pointer?.element)
                        .withIcon(icon)
                        .withTailText(tailText, true)
                }

                //currently only calculate from configs
                when (templateExpression.name) {
                    "system_scope" -> configGroup.systemScopes.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "localisation_locale" -> configGroup.localisationLocalesById.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "type" -> configGroup.types.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "subtype" -> {
                        val configPath = templateExpression.context.contextElement?.parentOfType<CwtMemberElement>(withSelf = true)?.configPath ?: return null
                        val type = when {
                            configPath.subPaths[0] == "types" -> configPath.subPaths.getOrNull(1)?.removeSurroundingOrNull("type[", "]") ?: return null
                            else -> return null
                        }
                        configGroup.types[type]?.subtypes?.mapToArray { (n, c) -> createLookupItem(n, c) }
                    }
                    "enum" -> configGroup.enums.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "complex_enum" -> configGroup.complexEnums.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "complex_enum_value" -> {
                        val configPath = templateExpression.context.contextElement?.parentOfType<CwtMemberElement>(withSelf = true)?.configPath ?: return null
                        val complexEnum = when {
                            configPath.subPaths[0] == "complex_enum_values" -> configPath.subPaths.getOrNull(1) ?: return null
                            else -> return null
                        }
                        configGroup.extendedComplexEnumValues[complexEnum]?.mapToArray { (n, c) -> createLookupItem(n, c) }
                    }
                    "dynamic_value_type" -> configGroup.dynamicValueTypes.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "dynamic_value" -> {
                        val configPath = templateExpression.context.contextElement?.parentOfType<CwtMemberElement>(withSelf = true)?.configPath ?: return null
                        val complexEnum = when {
                            configPath.subPaths[0] == "dynamic_values" -> configPath.subPaths.getOrNull(1) ?: return null
                            else -> return null
                        }
                        configGroup.extendedDynamicValues[complexEnum]?.mapToArray { (n, c) -> createLookupItem(n, c) }
                    }
                    "link" -> configGroup.links.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "scope" -> null //no completion yet
                    "localisation_link" -> configGroup.localisationLinks.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "localisation_command" -> configGroup.localisationCommands.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "modifier_category" -> configGroup.modifierCategories.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "modifier" -> configGroup.modifiers.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "scope_name" -> configGroup.scopes.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "scope_group" -> configGroup.scopeGroups.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "database_object_type" -> configGroup.databaseObjectTypes.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "scripted_variable" -> configGroup.extendedScriptedVariables.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "definition" -> configGroup.extendedDefinitions.mapToArray { (n, c) -> createLookupItem(n, c.singleOrNull()) }
                    "game_rule" -> configGroup.extendedGameRules.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "on_action" -> configGroup.extendedOnActions.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "inline_script" -> configGroup.extendedInlineScripts.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "parameter" -> configGroup.extendedParameters.mapToArray { (n, c) -> createLookupItem(n, c.singleOrNull()) }
                    "single_alias" -> configGroup.singleAliases.mapToArray { (n, c) -> createLookupItem(n, c) }
                    "alias_name" -> configGroup.aliasGroups.mapToArray { (n) -> createLookupItem(n) }
                    "alias_sub_name" -> {
                        val editor = context.editor ?: return null
                        val currentText = editor.document.charsSequence.substring(context.templateStartOffset, context.startOffset)
                        val aliasName = currentText.removeSurroundingOrNull("alias_name[", ":") ?: return null
                        configGroup.aliasGroups[aliasName]?.mapToArray { (n, c) -> createLookupItem(n, c.singleOrNull()) }
                    }
                    "inline" -> configGroup.inlineConfigGroup.mapToArray { (n, c) -> createLookupItem(n, c.singleOrNull()) }
                    else -> null
                }
            }
        }
    }

    //endregion
}
