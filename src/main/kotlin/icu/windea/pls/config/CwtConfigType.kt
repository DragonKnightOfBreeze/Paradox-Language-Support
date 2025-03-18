package icu.windea.pls.config

import icu.windea.pls.*
import icu.windea.pls.core.*
import javax.swing.*

enum class CwtConfigType(
    val id: String,
    val category: String? = null,
    val isReference: Boolean = false
) {
    Type("type") {
        override val prefix get() = PlsConstants.Strings.typePrefix
        override val icon get() = PlsIcons.Nodes.Type
    },
    Subtype("subtype") {
        override val prefix get() = PlsConstants.Strings.subtypePrefix
        override val icon get() = PlsIcons.Nodes.Type
    },
    Enum("enum") {
        override val prefix get() = PlsConstants.Strings.enumPrefix
        override val icon get() = PlsIcons.Nodes.Enum
    },
    EnumValue("enum value", "enums", isReference = true) {
        override val prefix get() = PlsConstants.Strings.enumValuePrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.enumValue")
        override val icon get() = PlsIcons.Nodes.EnumValue
    },
    ComplexEnum("complex enum") {
        override val prefix get() = PlsConstants.Strings.complexEnumPrefix
        override val icon get() = PlsIcons.Nodes.ComplexEnum
    },
    DynamicValueType("dynamic value type") {
        override val prefix get() = PlsConstants.Strings.dynamicValueTypePrefix
        override val icon get() = PlsIcons.Nodes.DynamicValueType
    },
    DynamicValue("dynamic value", "values", true) {
        override val prefix get() = PlsConstants.Strings.dynamicValuePrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.dynamicValue")
        override val icon get() = PlsIcons.Nodes.DynamicValue
    },
    Inline("inline") {
        override val prefix get() = PlsConstants.Strings.inlinePrefix
        override val icon get() = PlsIcons.Nodes.Inline
    },
    SingleAlias("single alias") {
        override val prefix get() = PlsConstants.Strings.singleAliasPrefix
        override val icon get() = PlsIcons.Nodes.Alias
    },
    Alias("alias") {
        override val prefix: String get() = PlsConstants.Strings.aliasPrefix
        override val icon get() = PlsIcons.Nodes.Alias
    },
    Link("link", isReference = true) {
        override val prefix get() = PlsConstants.Strings.linkPrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.link")
        override val icon get() = PlsIcons.Nodes.Link
    },
    LocalisationLink("localisation link", isReference = true) {
        override val prefix get() = PlsConstants.Strings.localisationLinkPrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.localisationLink")
        override val icon get() = PlsIcons.Nodes.Link
    },
    LocalisationPromotion("localisation promotion", isReference = true) {
        override val prefix get() = PlsConstants.Strings.localisationPromotionPrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.localisationPromotion")
        override val icon get() = PlsIcons.Nodes.Link
    },
    LocalisationCommand("localisation command", isReference = true) {
        override val prefix get() = PlsConstants.Strings.localisationCommandPrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.localisationCommand")
        override val icon get() = PlsIcons.LocalisationNodes.CommandField
    },
    ModifierCategory("modifier category", isReference = true) {
        override val prefix get() = PlsConstants.Strings.modifierCategoryPrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.modifierCategory")
        override val icon get() = PlsIcons.Nodes.ModifierCategory
    },
    Modifier("modifier", isReference = true) {
        override val prefix get() = PlsConstants.Strings.modifierPrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.modifier")
        override val icon get() = PlsIcons.Nodes.Modifier
    },
    Trigger("trigger", isReference = true) {
        override val prefix get() = PlsConstants.Strings.triggerPrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.trigger")
        override val icon get() = PlsIcons.Nodes.Trigger
    },
    Effect("effect", isReference = true) {
        override val prefix get() = PlsConstants.Strings.effectPrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.effect")
        override val icon get() = PlsIcons.Nodes.Effect
    },
    Scope("scope", isReference = true) {
        override val prefix get() = PlsConstants.Strings.scopePrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.scope")
        override val icon get() = PlsIcons.Nodes.Scope
    },
    ScopeGroup("scope group", isReference = true) {
        override val prefix get() = PlsConstants.Strings.scopeGroupPrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.scopeGroup")
        override val icon get() = PlsIcons.Nodes.Scope
    },
    DatabaseObjectType("database object type", isReference = true) {
        override val prefix get() = PlsConstants.Strings.databaseObjectTypePrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.databaseObjectType")
        override val icon get() = PlsIcons.Nodes.DatabaseObjectType
    },

    SystemScope("system scope", isReference = true) {
        override val prefix get() = PlsConstants.Strings.systemScopePrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.systemScope")
        override val icon get() = PlsIcons.Nodes.SystemScope
    },
    LocalisationLocale("localisation locale", isReference = true) {
        override val prefix get() = PlsConstants.Strings.localisationLocalePrefix
        override val descriptionText get() = PlsBundle.message("cwt.description.localisationLocale")
        override val icon get() = PlsIcons.LocalisationNodes.Locale
    },

    ExtendedScriptedVariable("extended scripted variable") {
        override val prefix get() = PlsConstants.Strings.scriptedVariablePrefix
        override val icon get() = PlsIcons.Nodes.ScriptedVariableConfig
    },
    ExtendedDefinition("extended definition") {
        override val prefix get() = PlsConstants.Strings.definitionPrefix
        override val icon get() = PlsIcons.Nodes.DefinitionConfig
    },
    ExtendedGameRule("extended game rule") {
        override val prefix get() = PlsConstants.Strings.gameRulePrefix
        override val icon get() = PlsIcons.Nodes.DefinitionConfig
    },
    ExtendedOnAction("extended on action") {
        override val prefix get() = PlsConstants.Strings.onActionPrefix
        override val icon get() = PlsIcons.Nodes.DefinitionConfig
    },
    ExtendedInlineScript("extended inline script") {
        override val prefix get() = PlsConstants.Strings.inlineScriptPrefix
        override val icon get() = PlsIcons.Nodes.InlineScriptConfig
    },
    ExtendedParameter("extended parameter") {
        override val prefix get() = PlsConstants.Strings.parameterPrefix
        override val icon get() = PlsIcons.Nodes.ParameterConfig
    },
    ExtendedDynamicValue("extended dynamic value") {
        override val prefix get() = PlsConstants.Strings.dynamicValuePrefix
        override val icon get() = PlsIcons.Nodes.DynamicValue
    },
    ExtendedComplexEnumValue("extended complex enum value") {
        override val prefix get() = PlsConstants.Strings.complexEnumValuePrefix
        override val icon get() = PlsIcons.Nodes.ComplexEnumValue
    },
    ;

    abstract val prefix: String
    open val descriptionText: String? = null
    open val icon: Icon? = null

    fun getShortName(name: String): String {
        //简单判断
        return when (this) {
            Type, Subtype, Enum, ComplexEnum, DynamicValueType -> name.substringIn('[', ']')
            Inline -> name.substringIn('[', ']')
            SingleAlias -> name.substringIn('[', ']')
            Alias, Trigger, Effect -> name.substringIn('[', ']').substringAfter(':')
            else -> name
        }
    }
}
