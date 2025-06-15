package icu.windea.pls.config

import icu.windea.pls.*
import icu.windea.pls.model.constants.PlsStringConstants

object CwtConfigTypes {
    val Type = CwtConfigType("type") {
        prefix = PlsStringConstants.typePrefix
        icon = PlsIcons.Nodes.Type
    }
    val Subtype = CwtConfigType("subtype") {
        prefix = PlsStringConstants.subtypePrefix
        icon = PlsIcons.Nodes.Type
    }
    val Enum = CwtConfigType("enum") {
        prefix = PlsStringConstants.enumPrefix
        icon = PlsIcons.Nodes.Enum
    }
    val EnumValue = CwtConfigType("enum value", category = "enums", isReference = true) {
        prefix = PlsStringConstants.enumValuePrefix
        description = PlsBundle.message("cwt.description.enumValue")
        icon = PlsIcons.Nodes.EnumValue
    }
    val ComplexEnum = CwtConfigType("complex enum") {
        prefix = PlsStringConstants.complexEnumPrefix
        icon = PlsIcons.Nodes.Enum
    }
    val DynamicValueType = CwtConfigType("dynamic value type") {
        prefix = PlsStringConstants.dynamicValueTypePrefix
        icon = PlsIcons.Nodes.DynamicValueType
    }
    val DynamicValue = CwtConfigType("dynamic value", category = "values", isReference = true) {
        prefix = PlsStringConstants.dynamicValuePrefix
        description = PlsBundle.message("cwt.description.dynamicValue")
        icon = PlsIcons.Nodes.DynamicValue
    }
    val Inline = CwtConfigType("inline") {
        prefix = PlsStringConstants.inlinePrefix
        icon = PlsIcons.Nodes.InlineScript
    }
    val SingleAlias = CwtConfigType("single alias") {
        prefix = PlsStringConstants.singleAliasPrefix
        icon = PlsIcons.Nodes.Alias
    }
    val Alias = CwtConfigType("alias") {
        prefix = PlsStringConstants.aliasPrefix
        icon = PlsIcons.Nodes.Alias
    }
    val Link = CwtConfigType("link", isReference = true) {
        prefix = PlsStringConstants.linkPrefix
        description = PlsBundle.message("cwt.description.link")
        icon = PlsIcons.Nodes.Link
    }
    val LocalisationLink = CwtConfigType("localisation link", isReference = true) {
        prefix = PlsStringConstants.localisationLinkPrefix
        description = PlsBundle.message("cwt.description.localisationLink")
        icon = PlsIcons.Nodes.Link
    }
    val LocalisationPromotion = CwtConfigType("localisation promotion", isReference = true) {
        prefix = PlsStringConstants.localisationPromotionPrefix
        description = PlsBundle.message("cwt.description.localisationPromotion")
        icon = PlsIcons.Nodes.Link
    }
    val LocalisationCommand = CwtConfigType("localisation command", isReference = true) {
        prefix = PlsStringConstants.localisationCommandPrefix
        description = PlsBundle.message("cwt.description.localisationCommand")
        icon = PlsIcons.Nodes.LocalisationCommandField
    }
    val ModifierCategory = CwtConfigType("modifier category", isReference = true) {
        prefix = PlsStringConstants.modifierCategoryPrefix
        description = PlsBundle.message("cwt.description.modifierCategory")
        icon = PlsIcons.Nodes.ModifierCategory
    }
    val Modifier = CwtConfigType("modifier", isReference = true) {
        prefix = PlsStringConstants.modifierPrefix
        description = PlsBundle.message("cwt.description.modifier")
        icon = PlsIcons.Nodes.Modifier
    }
    val Trigger = CwtConfigType("trigger", isReference = true) {
        prefix = PlsStringConstants.triggerPrefix
        description = PlsBundle.message("cwt.description.trigger")
        icon = PlsIcons.Nodes.Trigger
    }
    val Effect = CwtConfigType("effect", isReference = true) {
        prefix = PlsStringConstants.effectPrefix
        description = PlsBundle.message("cwt.description.effect")
        icon = PlsIcons.Nodes.Effect
    }
    val Scope = CwtConfigType("scope", isReference = true) {
        prefix = PlsStringConstants.scopePrefix
        description = PlsBundle.message("cwt.description.scope")
        icon = PlsIcons.Nodes.Scope
    }
    val ScopeGroup = CwtConfigType("scope group", isReference = true) {
        prefix = PlsStringConstants.scopeGroupPrefix
        description = PlsBundle.message("cwt.description.scopeGroup")
        icon = PlsIcons.Nodes.Scope
    }
    val DatabaseObjectType = CwtConfigType("database object type", isReference = true) {
        prefix = PlsStringConstants.databaseObjectTypePrefix
        description = PlsBundle.message("cwt.description.databaseObjectType")
        icon = PlsIcons.Nodes.DatabaseObjectType
    }
    val SystemScope = CwtConfigType("system scope", isReference = true) {
        prefix = PlsStringConstants.systemScopePrefix
        description = PlsBundle.message("cwt.description.systemScope")
        icon = PlsIcons.Nodes.SystemScope
    }
    val Locale = CwtConfigType("locale", isReference = true) {
        prefix = PlsStringConstants.localePrefix
        description = PlsBundle.message("cwt.description.locale")
        icon = PlsIcons.Nodes.LocalisationLocale
    }

    val ExtendedScriptedVariable = CwtConfigType("extended scripted variable") {
        prefix = PlsStringConstants.scriptedVariablePrefix
        icon = PlsIcons.Nodes.ScriptedVariableConfig
    }
    val ExtendedDefinition = CwtConfigType("extended definition") {
        prefix = PlsStringConstants.definitionPrefix
        icon = PlsIcons.Nodes.DefinitionConfig
    }
    val ExtendedGameRule = CwtConfigType("extended game rule") {
        prefix = PlsStringConstants.gameRulePrefix
        icon = PlsIcons.Nodes.DefinitionConfig
    }
    val ExtendedOnAction = CwtConfigType("extended on action") {
        prefix = PlsStringConstants.onActionPrefix
        icon = PlsIcons.Nodes.DefinitionConfig
    }
    val ExtendedInlineScript = CwtConfigType("extended inline script") {
        prefix = PlsStringConstants.inlineScriptPrefix
        icon = PlsIcons.Nodes.InlineScriptConfig
    }
    val ExtendedParameter = CwtConfigType("extended parameter") {
        prefix = PlsStringConstants.parameterPrefix
        icon = PlsIcons.Nodes.ParameterConfig
    }
    val ExtendedDynamicValue = CwtConfigType("extended dynamic value") {
        prefix = PlsStringConstants.dynamicValuePrefix
        icon = PlsIcons.Nodes.DynamicValueConfig
    }
    val ExtendedComplexEnumValue = CwtConfigType("extended complex enum value") {
        prefix = PlsStringConstants.complexEnumValuePrefix
        icon = PlsIcons.Nodes.EnumValueConfig
    }
}
