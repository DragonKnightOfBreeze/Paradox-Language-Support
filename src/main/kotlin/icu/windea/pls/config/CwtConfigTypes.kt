package icu.windea.pls.config

import icu.windea.pls.*
import icu.windea.pls.model.constants.*

object CwtConfigTypes {
    val Type = CwtConfigType.create("type") {
        icon = PlsIcons.Nodes.Type
        prefix = PlsStringConstants.typePrefix
    }
    val Subtype = CwtConfigType.create("subtype") {
        icon = PlsIcons.Nodes.Type
        prefix = PlsStringConstants.subtypePrefix
    }
    val Enum = CwtConfigType.create("enum") {
        icon = PlsIcons.Nodes.Enum
        prefix = PlsStringConstants.enumPrefix
    }
    val ComplexEnum = CwtConfigType.create("complex enum") {
        icon = PlsIcons.Nodes.Enum
        prefix = PlsStringConstants.complexEnumPrefix
    }
    val EnumValue = CwtConfigType.create("enum value", category = "enums", isReference = true) {
        icon = PlsIcons.Nodes.EnumValue
        prefix = PlsStringConstants.enumValuePrefix
        description = PlsBundle.message("cwt.description.enumValue")
    }
    val DynamicValueType = CwtConfigType.create("dynamic value type") {
        icon = PlsIcons.Nodes.DynamicValueType
        prefix = PlsStringConstants.dynamicValueTypePrefix
    }
    val DynamicValue = CwtConfigType.create("dynamic value", category = "values", isReference = true) {
        icon = PlsIcons.Nodes.DynamicValue
        prefix = PlsStringConstants.dynamicValuePrefix
        description = PlsBundle.message("cwt.description.dynamicValue")
    }
    val Inline = CwtConfigType.create("inline") {
        icon = PlsIcons.Nodes.InlineScript
        prefix = PlsStringConstants.inlinePrefix
    }
    val SingleAlias = CwtConfigType.create("single alias") {
        icon = PlsIcons.Nodes.Alias
        prefix = PlsStringConstants.singleAliasPrefix
    }
    val Alias = CwtConfigType.create("alias") {
        icon = PlsIcons.Nodes.Alias
        prefix = PlsStringConstants.aliasPrefix
    }
    val Link = CwtConfigType.create("link", isReference = true) {
        icon = PlsIcons.Nodes.Link
        prefix = PlsStringConstants.linkPrefix
        description = PlsBundle.message("cwt.description.link")
    }
    val LocalisationLink = CwtConfigType.create("localisation link", isReference = true) {
        icon = PlsIcons.Nodes.Link
        prefix = PlsStringConstants.localisationLinkPrefix
        description = PlsBundle.message("cwt.description.localisationLink")
    }
    val LocalisationPromotion = CwtConfigType.create("localisation promotion", isReference = true) {
        icon = PlsIcons.Nodes.Link
        prefix = PlsStringConstants.localisationPromotionPrefix
        description = PlsBundle.message("cwt.description.localisationPromotion")
    }
    val LocalisationCommand = CwtConfigType.create("localisation command", isReference = true) {
        icon = PlsIcons.Nodes.LocalisationCommandField
        prefix = PlsStringConstants.localisationCommandPrefix
        description = PlsBundle.message("cwt.description.localisationCommand")
    }
    val ModifierCategory = CwtConfigType.create("modifier category", isReference = true) {
        icon = PlsIcons.Nodes.ModifierCategory
        prefix = PlsStringConstants.modifierCategoryPrefix
        description = PlsBundle.message("cwt.description.modifierCategory")
    }
    val Modifier = CwtConfigType.create("modifier", isReference = true) {
        icon = PlsIcons.Nodes.Modifier
        prefix = PlsStringConstants.modifierPrefix
        description = PlsBundle.message("cwt.description.modifier")
    }
    val Trigger = CwtConfigType.create("trigger", isReference = true) {
        icon = PlsIcons.Nodes.Trigger
        prefix = PlsStringConstants.triggerPrefix
        description = PlsBundle.message("cwt.description.trigger")
    }
    val Effect = CwtConfigType.create("effect", isReference = true) {
        icon = PlsIcons.Nodes.Effect
        prefix = PlsStringConstants.effectPrefix
        description = PlsBundle.message("cwt.description.effect")
    }
    val Scope = CwtConfigType.create("scope", isReference = true) {
        icon = PlsIcons.Nodes.Scope
        prefix = PlsStringConstants.scopePrefix
        description = PlsBundle.message("cwt.description.scope")
    }
    val ScopeGroup = CwtConfigType.create("scope group", isReference = true) {
        icon = PlsIcons.Nodes.Scope
        prefix = PlsStringConstants.scopeGroupPrefix
        description = PlsBundle.message("cwt.description.scopeGroup")
    }
    val DatabaseObjectType = CwtConfigType.create("database object type", isReference = true) {
        icon = PlsIcons.Nodes.DatabaseObjectType
        prefix = PlsStringConstants.databaseObjectTypePrefix
        description = PlsBundle.message("cwt.description.databaseObjectType")
    }
    val SystemScope = CwtConfigType.create("system scope", isReference = true) {
        icon = PlsIcons.Nodes.SystemScope
        prefix = PlsStringConstants.systemScopePrefix
        description = PlsBundle.message("cwt.description.systemScope")
    }
    val Locale = CwtConfigType.create("locale", isReference = true) {
        icon = PlsIcons.Nodes.LocalisationLocale
        prefix = PlsStringConstants.localePrefix
        description = PlsBundle.message("cwt.description.locale")
    }

    val ExtendedScriptedVariable = CwtConfigType.create("extended scripted variable") {
        icon = PlsIcons.Nodes.ScriptedVariableConfig
        prefix = PlsStringConstants.scriptedVariablePrefix
    }
    val ExtendedDefinition = CwtConfigType.create("extended definition") {
        icon = PlsIcons.Nodes.DefinitionConfig
        prefix = PlsStringConstants.definitionPrefix
    }
    val ExtendedGameRule = CwtConfigType.create("extended game rule") {
        icon = PlsIcons.Nodes.DefinitionConfig
        prefix = PlsStringConstants.gameRulePrefix
    }
    val ExtendedOnAction = CwtConfigType.create("extended on action") {
        icon = PlsIcons.Nodes.DefinitionConfig
        prefix = PlsStringConstants.onActionPrefix
    }
    val ExtendedInlineScript = CwtConfigType.create("extended inline script") {
        icon = PlsIcons.Nodes.InlineScriptConfig
        prefix = PlsStringConstants.inlineScriptPrefix
    }
    val ExtendedParameter = CwtConfigType.create("extended parameter") {
        icon = PlsIcons.Nodes.ParameterConfig
        prefix = PlsStringConstants.parameterPrefix
    }
    val ExtendedDynamicValue = CwtConfigType.create("extended dynamic value") {
        icon = PlsIcons.Nodes.DynamicValueConfig
        prefix = PlsStringConstants.dynamicValuePrefix
    }
    val ExtendedComplexEnumValue = CwtConfigType.create("extended complex enum value") {
        icon = PlsIcons.Nodes.EnumValueConfig
        prefix = PlsStringConstants.complexEnumValuePrefix
    }
}
