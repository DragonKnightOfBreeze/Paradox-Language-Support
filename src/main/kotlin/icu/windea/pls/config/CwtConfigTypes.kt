package icu.windea.pls.config

import icu.windea.pls.*

object CwtConfigTypes {
    val Type = CwtConfigType("type") {
        prefix = PlsConstants.Strings.typePrefix
        icon = PlsIcons.Nodes.Type
    }
    val Subtype = CwtConfigType("subtype") {
        prefix = PlsConstants.Strings.subtypePrefix
        icon = PlsIcons.Nodes.Type
    }
    val Enum = CwtConfigType("enum") {
        prefix = PlsConstants.Strings.enumPrefix
        icon = PlsIcons.Nodes.Enum
    }
    val EnumValue = CwtConfigType("enum value", category = "enums", isReference = true) {
        prefix = PlsConstants.Strings.enumValuePrefix
        description = PlsBundle.message("cwt.description.enumValue")
        icon = PlsIcons.Nodes.EnumValue
    }
    val ComplexEnum = CwtConfigType("complex enum") {
        prefix = PlsConstants.Strings.complexEnumPrefix
        icon = PlsIcons.Nodes.Enum
    }
    val DynamicValueType = CwtConfigType("dynamic value type") {
        prefix = PlsConstants.Strings.dynamicValueTypePrefix
        icon = PlsIcons.Nodes.DynamicValueType
    }
    val DynamicValue = CwtConfigType("dynamic value", category = "values", isReference = true) {
        prefix = PlsConstants.Strings.dynamicValuePrefix
        description = PlsBundle.message("cwt.description.dynamicValue")
        icon = PlsIcons.Nodes.DynamicValue
    }
    val Inline = CwtConfigType("inline") {
        prefix = PlsConstants.Strings.inlinePrefix
        icon = PlsIcons.Nodes.InlineScript
    }
    val SingleAlias = CwtConfigType("single alias") {
        prefix = PlsConstants.Strings.singleAliasPrefix
        icon = PlsIcons.Nodes.Alias
    }
    val Alias = CwtConfigType("alias") {
        prefix = PlsConstants.Strings.aliasPrefix
        icon = PlsIcons.Nodes.Alias
    }
    val Link = CwtConfigType("link", isReference = true) {
        prefix = PlsConstants.Strings.linkPrefix
        description = PlsBundle.message("cwt.description.link")
        icon = PlsIcons.Nodes.Link
    }
    val LocalisationLink = CwtConfigType("localisation link", isReference = true) {
        prefix = PlsConstants.Strings.localisationLinkPrefix
        description = PlsBundle.message("cwt.description.localisationLink")
        icon = PlsIcons.Nodes.Link
    }
    val LocalisationPromotion = CwtConfigType("localisation promotion", isReference = true) {
        prefix = PlsConstants.Strings.localisationPromotionPrefix
        description = PlsBundle.message("cwt.description.localisationPromotion")
        icon = PlsIcons.Nodes.Link
    }
    val LocalisationCommand = CwtConfigType("localisation command", isReference = true) {
        prefix = PlsConstants.Strings.localisationCommandPrefix
        description = PlsBundle.message("cwt.description.localisationCommand")
        icon = PlsIcons.Nodes.LocalisationCommandField
    }
    val ModifierCategory = CwtConfigType("modifier category", isReference = true) {
        prefix = PlsConstants.Strings.modifierCategoryPrefix
        description = PlsBundle.message("cwt.description.modifierCategory")
        icon = PlsIcons.Nodes.ModifierCategory
    }
    val Modifier = CwtConfigType("modifier", isReference = true) {
        prefix = PlsConstants.Strings.modifierPrefix
        description = PlsBundle.message("cwt.description.modifier")
        icon = PlsIcons.Nodes.Modifier
    }
    val Trigger = CwtConfigType("trigger", isReference = true) {
        prefix = PlsConstants.Strings.triggerPrefix
        description = PlsBundle.message("cwt.description.trigger")
        icon = PlsIcons.Nodes.Trigger
    }
    val Effect = CwtConfigType("effect", isReference = true) {
        prefix = PlsConstants.Strings.effectPrefix
        description = PlsBundle.message("cwt.description.effect")
        icon = PlsIcons.Nodes.Effect
    }
    val Scope = CwtConfigType("scope", isReference = true) {
        prefix = PlsConstants.Strings.scopePrefix
        description = PlsBundle.message("cwt.description.scope")
        icon = PlsIcons.Nodes.Scope
    }
    val ScopeGroup = CwtConfigType("scope group", isReference = true) {
        prefix = PlsConstants.Strings.scopeGroupPrefix
        description = PlsBundle.message("cwt.description.scopeGroup")
        icon = PlsIcons.Nodes.Scope
    }
    val DatabaseObjectType = CwtConfigType("database object type", isReference = true) {
        prefix = PlsConstants.Strings.databaseObjectTypePrefix
        description = PlsBundle.message("cwt.description.databaseObjectType")
        icon = PlsIcons.Nodes.DatabaseObjectType
    }
    val SystemScope = CwtConfigType("system scope", isReference = true) {
        prefix = PlsConstants.Strings.systemScopePrefix
        description = PlsBundle.message("cwt.description.systemScope")
        icon = PlsIcons.Nodes.SystemScope
    }
    val Locale = CwtConfigType("locale", isReference = true) {
        prefix = PlsConstants.Strings.localePrefix
        description = PlsBundle.message("cwt.description.locale")
        icon = PlsIcons.Nodes.LocalisationLocale
    }

    val ExtendedScriptedVariable = CwtConfigType("extended scripted variable") {
        prefix = PlsConstants.Strings.scriptedVariablePrefix
        icon = PlsIcons.Nodes.ScriptedVariableConfig
    }
    val ExtendedDefinition = CwtConfigType("extended definition") {
        prefix = PlsConstants.Strings.definitionPrefix
        icon = PlsIcons.Nodes.DefinitionConfig
    }
    val ExtendedGameRule = CwtConfigType("extended game rule") {
        prefix = PlsConstants.Strings.gameRulePrefix
        icon = PlsIcons.Nodes.DefinitionConfig
    }
    val ExtendedOnAction = CwtConfigType("extended on action") {
        prefix = PlsConstants.Strings.onActionPrefix
        icon = PlsIcons.Nodes.DefinitionConfig
    }
    val ExtendedInlineScript = CwtConfigType("extended inline script") {
        prefix = PlsConstants.Strings.inlineScriptPrefix
        icon = PlsIcons.Nodes.InlineScriptConfig
    }
    val ExtendedParameter = CwtConfigType("extended parameter") {
        prefix = PlsConstants.Strings.parameterPrefix
        icon = PlsIcons.Nodes.ParameterConfig
    }
    val ExtendedDynamicValue = CwtConfigType("extended dynamic value") {
        prefix = PlsConstants.Strings.dynamicValuePrefix
        icon = PlsIcons.Nodes.DynamicValueConfig
    }
    val ExtendedComplexEnumValue = CwtConfigType("extended complex enum value") {
        prefix = PlsConstants.Strings.complexEnumValuePrefix
        icon = PlsIcons.Nodes.EnumValueConfig
    }
}
