package icu.windea.pls.config

import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.model.constants.PlsStrings

object CwtConfigTypes {
    val Type = CwtConfigType.create("type") {
        icon = PlsIcons.Nodes.Type
        prefix = PlsStrings.typePrefix
        description = PlsBundle.message("cwt.config.description.type")
    }
    val Subtype = CwtConfigType.create("subtype") {
        icon = PlsIcons.Nodes.Type
        prefix = PlsStrings.subtypePrefix
        description = PlsBundle.message("cwt.config.description.subtype")
    }
    val Row = CwtConfigType.create("row") {
        icon = PlsIcons.Nodes.Row
        prefix = PlsStrings.rowPrefix
        description = PlsBundle.message("cwt.config.description.row")
    }
    val Enum = CwtConfigType.create("enum") {
        icon = PlsIcons.Nodes.Enum
        prefix = PlsStrings.enumPrefix
        description = PlsBundle.message("cwt.config.description.enum")
    }
    val ComplexEnum = CwtConfigType.create("complex enum") {
        icon = PlsIcons.Nodes.Enum
        prefix = PlsStrings.complexEnumPrefix
        description = PlsBundle.message("cwt.config.description.complexEnum")
    }
    val EnumValue = CwtConfigType.create("enum value", category = "enums", isReference = true) {
        icon = PlsIcons.Nodes.EnumValue
        prefix = PlsStrings.enumValuePrefix
        description = PlsBundle.message("cwt.config.description.enumValue")
    }
    val DynamicValueType = CwtConfigType.create("dynamic value type") {
        icon = PlsIcons.Nodes.DynamicValueType
        prefix = PlsStrings.dynamicValueTypePrefix
        description = PlsBundle.message("cwt.config.description.dynamicValueType")
    }
    val DynamicValue = CwtConfigType.create("dynamic value", category = "values", isReference = true) {
        icon = PlsIcons.Nodes.DynamicValue
        prefix = PlsStrings.dynamicValuePrefix
        description = PlsBundle.message("cwt.config.description.dynamicValue")
    }
    val SingleAlias = CwtConfigType.create("single alias") {
        icon = PlsIcons.Nodes.Alias
        prefix = PlsStrings.singleAliasPrefix
        description = PlsBundle.message("cwt.config.description.singleAlias")
    }
    val Alias = CwtConfigType.create("alias") {
        icon = PlsIcons.Nodes.Alias
        prefix = PlsStrings.aliasPrefix
        description = PlsBundle.message("cwt.config.description.alias")
    }
    val Directive = CwtConfigType.create("directive") {
        icon = PlsIcons.Nodes.Directive
        prefix = PlsStrings.directivePrefix
        description = PlsBundle.message("cwt.config.description.directive")
    }
    val Link = CwtConfigType.create("link", isReference = true) {
        icon = PlsIcons.Nodes.Link
        prefix = PlsStrings.linkPrefix
        description = PlsBundle.message("cwt.config.description.link")
    }
    val LocalisationLink = CwtConfigType.create("localisation link", isReference = true) {
        icon = PlsIcons.Nodes.Link
        prefix = PlsStrings.localisationLinkPrefix
        description = PlsBundle.message("cwt.config.description.localisationLink")
    }
    val LocalisationPromotion = CwtConfigType.create("localisation promotion", isReference = true) {
        icon = PlsIcons.Nodes.Link
        prefix = PlsStrings.localisationPromotionPrefix
        description = PlsBundle.message("cwt.config.description.localisationPromotion")
    }
    val LocalisationCommand = CwtConfigType.create("localisation command", isReference = true) {
        icon = PlsIcons.Nodes.LocalisationCommandField
        prefix = PlsStrings.localisationCommandPrefix
        description = PlsBundle.message("cwt.config.description.localisationCommand")
    }
    val ModifierCategory = CwtConfigType.create("modifier category", isReference = true) {
        icon = PlsIcons.Nodes.ModifierCategory
        prefix = PlsStrings.modifierCategoryPrefix
        description = PlsBundle.message("cwt.config.description.modifierCategory")
    }
    val Modifier = CwtConfigType.create("modifier", isReference = true) {
        icon = PlsIcons.Nodes.Modifier
        prefix = PlsStrings.modifierPrefix
        description = PlsBundle.message("cwt.config.description.modifier")
    }
    val Trigger = CwtConfigType.create("trigger", isReference = true) {
        icon = PlsIcons.Nodes.Trigger
        prefix = PlsStrings.triggerPrefix
        description = PlsBundle.message("cwt.config.description.trigger")
    }
    val Effect = CwtConfigType.create("effect", isReference = true) {
        icon = PlsIcons.Nodes.Effect
        prefix = PlsStrings.effectPrefix
        description = PlsBundle.message("cwt.config.description.effect")
    }
    val Scope = CwtConfigType.create("scope", isReference = true) {
        icon = PlsIcons.Nodes.Scope
        prefix = PlsStrings.scopePrefix
        description = PlsBundle.message("cwt.config.description.scope")
    }
    val ScopeGroup = CwtConfigType.create("scope group", isReference = true) {
        icon = PlsIcons.Nodes.Scope
        prefix = PlsStrings.scopeGroupPrefix
        description = PlsBundle.message("cwt.config.description.scopeGroup")
    }
    val DatabaseObjectType = CwtConfigType.create("database object type", isReference = true) {
        icon = PlsIcons.Nodes.DatabaseObjectType
        prefix = PlsStrings.databaseObjectTypePrefix
        description = PlsBundle.message("cwt.config.description.databaseObjectType")
    }
    val SystemScope = CwtConfigType.create("system scope", isReference = true) {
        icon = PlsIcons.Nodes.SystemScope
        prefix = PlsStrings.systemScopePrefix
        description = PlsBundle.message("cwt.config.description.systemScope")
    }
    val Locale = CwtConfigType.create("locale", isReference = true) {
        icon = PlsIcons.Nodes.LocalisationLocale
        prefix = PlsStrings.localePrefix
        description = PlsBundle.message("cwt.config.description.locale")
    }

    val ExtendedScriptedVariable = CwtConfigType.create("extended scripted variable") {
        icon = PlsIcons.Nodes.ScriptedVariableConfig
        prefix = PlsStrings.scriptedVariablePrefix
    }
    val ExtendedDefinition = CwtConfigType.create("extended definition") {
        icon = PlsIcons.Nodes.DefinitionConfig
        prefix = PlsStrings.definitionPrefix
    }
    val ExtendedGameRule = CwtConfigType.create("extended game rule") {
        icon = PlsIcons.Nodes.DefinitionConfig
        prefix = PlsStrings.gameRulePrefix
    }
    val ExtendedOnAction = CwtConfigType.create("extended on action") {
        icon = PlsIcons.Nodes.DefinitionConfig
        prefix = PlsStrings.onActionPrefix
    }
    val ExtendedInlineScript = CwtConfigType.create("extended inline script") {
        icon = PlsIcons.Nodes.InlineScriptConfig
        prefix = PlsStrings.inlineScriptPrefix
    }
    val ExtendedParameter = CwtConfigType.create("extended parameter") {
        icon = PlsIcons.Nodes.ParameterConfig
        prefix = PlsStrings.parameterPrefix
    }
    val ExtendedDynamicValue = CwtConfigType.create("extended dynamic value") {
        icon = PlsIcons.Nodes.DynamicValueConfig
        prefix = PlsStrings.dynamicValuePrefix
    }
    val ExtendedComplexEnumValue = CwtConfigType.create("extended complex enum value") {
        icon = PlsIcons.Nodes.EnumValueConfig
        prefix = PlsStrings.complexEnumValuePrefix
    }
}
