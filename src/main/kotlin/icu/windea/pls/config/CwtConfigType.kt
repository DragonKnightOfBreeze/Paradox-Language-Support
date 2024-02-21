package icu.windea.pls.config

import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import javax.swing.*

enum class CwtConfigType(
	val id: String,
	val isReference: Boolean = false,
	val category: String? = null
) {
	Type("type") {
		override val prefix get() = PlsBundle.message("prefix.type")
		override val icon get() = PlsIcons.Nodes.Type
	},
	Subtype("subtype") {
		override val prefix get() = PlsBundle.message("prefix.subtype")
		override val icon get() = PlsIcons.Nodes.Type
	},
	Enum("enum") {
		override val prefix get() = PlsBundle.message("prefix.enum")
		override val icon get() = PlsIcons.Nodes.Enum
	},
	EnumValue("enum value", true, "enums") {
		override val prefix get() = PlsBundle.message("prefix.enumValue")
		override val descriptionText get() = PlsBundle.message("cwt.description.enumValue")
		override val icon get() = PlsIcons.Nodes.EnumValue
	},
	ComplexEnum("complex enum") {
		override val prefix get() = PlsBundle.message("prefix.complexEnum")
		override val icon get() = PlsIcons.Nodes.ComplexEnum
	},
	DynamicValueType("dynamic value type") {
		override val prefix get() = PlsBundle.message("prefix.dynamicValueType")
		override val icon get() = PlsIcons.Nodes.DynamicValueType
	},
	DynamicValue("dynamic value", true, "values") {
		override val prefix get() = PlsBundle.message("prefix.dynamicValue")
		override val descriptionText get() = PlsBundle.message("cwt.description.dynamicValue")
		override val icon get() = PlsIcons.Nodes.DynamicValue
	},
	Inline("inline") {
		override val prefix get() = PlsBundle.message("prefix.inline")
		override val icon get() = PlsIcons.Nodes.Inline
	},
	SingleAlias("single alias") {
		override val prefix get() = PlsBundle.message("prefix.singleAlias")
		override val icon get() = PlsIcons.Nodes.Alias
	},
	Alias("alias") {
		override val prefix: String get() = PlsBundle.message("prefix.alias")
		override val icon get() = PlsIcons.Nodes.Alias
	},
	Link("link", true) {
		override val prefix get() = PlsBundle.message("prefix.link")
		override val descriptionText get() = PlsBundle.message("cwt.description.link")
		override val icon get() = PlsIcons.Nodes.Link
	},
	LocalisationLink("localisation link", true) {
		override val prefix get() = PlsBundle.message("prefix.localisationLink")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationLink")
		override val icon get() = PlsIcons.Nodes.Link
	},
	LocalisationCommand("localisation command", true) {
		override val prefix get() = PlsBundle.message("prefix.localisationCommand")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationCommand")
		override val icon get() = PlsIcons.LocalisationNodes.CommandField
	},
	ModifierCategory("modifier category", true) {
		override val prefix get() = PlsBundle.message("prefix.modifierCategory")
		override val descriptionText get() = PlsBundle.message("cwt.description.modifierCategory")
		override val icon get() = PlsIcons.Nodes.ModifierCategory
	},
	Modifier("modifier", true) {
		override val prefix get() = PlsBundle.message("prefix.modifier")
		override val descriptionText get() = PlsBundle.message("cwt.description.modifier")
		override val icon get() = PlsIcons.Nodes.Modifier
	},
	Trigger("trigger", true) {
		override val prefix get() = PlsBundle.message("prefix.trigger")
		override val descriptionText get() = PlsBundle.message("cwt.description.trigger")
		override val icon get() = PlsIcons.Nodes.Trigger
	},
	Effect("effect", true) {
		override val prefix get() = PlsBundle.message("prefix.effect")
		override val descriptionText get() = PlsBundle.message("cwt.description.effect")
		override val icon get() = PlsIcons.Nodes.Effect
	},
	ScopeGroup("scope group", true) {
		override val prefix get() = PlsBundle.message("prefix.scopeGroup")
		override val descriptionText get() = PlsBundle.message("cwt.description.scopeGroup")
		override val icon get() = PlsIcons.Nodes.Scope
	},
	Scope("scope", true) {
		override val prefix get() = PlsBundle.message("prefix.scope")
		override val descriptionText get() = PlsBundle.message("cwt.description.scope")
		override val icon get() = PlsIcons.Nodes.Scope
	},
	SystemLink("system link", true) {
		override val prefix get() = PlsBundle.message("prefix.systemLink")
		override val descriptionText get() = PlsBundle.message("cwt.description.systemLink")
		override val icon get() = PlsIcons.Nodes.SystemScope
	},
	LocalisationLocale("localisation locale", true) {
		override val prefix get() = PlsBundle.message("prefix.localisationLocale")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationLocale")
		override val icon get() = PlsIcons.LocalisationNodes.Locale
	},
	LocalisationPredefinedParameter("localisation predefined parameter", true) {
		override val prefix get() = PlsBundle.message("prefix.localisationPredefinedParameter")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationPredefinedParameter")
		override val icon get() = PlsIcons.Nodes.PredefinedParameter
	},
	Definition("definition") {
		override val prefix get() = PlsBundle.message("prefix.definition")
		override val icon get() = PlsIcons.Nodes.DefinitionConfig
	},
	GameRule("game rule") {
		override val prefix get() = PlsBundle.message("prefix.gameRule")
		override val icon get() = PlsIcons.Nodes.DefinitionConfig
	},
	OnAction("on action") {
		override val prefix get() = PlsBundle.message("prefix.onAction")
		override val icon get() = PlsIcons.Nodes.DefinitionConfig
	},
	;
	
	abstract val prefix: String
	open val descriptionText: String? = null
	open val icon: Icon? = null
	
	fun getShortName(name: String) : String{
		//简单判断
		return when(this) {
			Type, Subtype, Enum, ComplexEnum, DynamicValueType -> name.substringIn('[',']')
			Inline -> name.substringIn('[',']')
			SingleAlias -> name.substringIn('[',']')
			Alias, Trigger, Effect -> name.substringIn('[',']').substringAfter(':')
			else -> name
		}
	}
}
