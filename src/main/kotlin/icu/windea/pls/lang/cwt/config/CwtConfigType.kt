package icu.windea.pls.lang.cwt.config

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
		override val icon get() = PlsIcons.Type
	},
	Subtype("subtype") {
		override val prefix get() = PlsBundle.message("prefix.subtype")
		override val icon get() = PlsIcons.Type
	},
	Enum("enum") {
		override val prefix get() = PlsBundle.message("prefix.enum")
	},
	ComplexEnum("complex enum") {
		override val prefix get() = PlsBundle.message("prefix.complexEnum")
	},
	ValueSet("value") {
		override val prefix get() = PlsBundle.message("prefix.valueSet")
	},
	OnAction("on action") {
		override val prefix get() = PlsBundle.message("prefix.onAction")
		override val icon get() = PlsIcons.OnAction
	},
	SingleAlias("single alias") {
		override val prefix get() = PlsBundle.message("prefix.singleAlias")
		override val icon get() =  PlsIcons.Alias
	},
	Alias("alias") {
		override val prefix: String get() = PlsBundle.message("prefix.alias")
		override val icon get() =  PlsIcons.Alias
	},
	EnumValue("enum value", true, "enums") {
		override val prefix get() = PlsBundle.message("prefix.enumValue")
		override val descriptionText get() = PlsBundle.message("cwt.description.enumValue")
		override val icon get() =  PlsIcons.EnumValue
	},
	ValueSetValue("value set value", true, "values") {
		override val prefix get() = PlsBundle.message("prefix.valueSetValue")
		override val descriptionText get() = PlsBundle.message("cwt.description.valueSetValue")
		override val icon get() =  PlsIcons.ValueSetValue
	},
	Link("link", true) {
		override val prefix get() = PlsBundle.message("prefix.link")
		override val descriptionText get() = PlsBundle.message("cwt.description.link")
		override val icon get() = PlsIcons.Link
	},
	LocalisationLink("localisation link", true) {
		override val prefix get() = PlsBundle.message("prefix.localisationLink")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationLink")
		override val icon get() = PlsIcons.Link
	},
	LocalisationCommand("localisation command", true) {
		override val prefix get() = PlsBundle.message("prefix.localisationCommand")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationCommand")
		override val icon get() = PlsIcons.LocalisationCommandField
	},
	ModifierCategory("modifier category", true) {
		override val prefix get() = PlsBundle.message("prefix.modifierCategory")
		override val descriptionText get() = PlsBundle.message("cwt.description.modifierCategory")
		override val icon get() = PlsIcons.ModifierCategory
	},
	Modifier("modifier", true) {
		override val prefix get() = PlsBundle.message("prefix.modifier")
		override val descriptionText get() = PlsBundle.message("cwt.description.modifier")
		override val icon get() = PlsIcons.Modifier
	},
	Trigger("trigger", true) {
		override val prefix get() = PlsBundle.message("prefix.trigger")
		override val descriptionText get() = PlsBundle.message("cwt.description.trigger")
		override val icon get() = PlsIcons.Trigger
	},
	Effect("effect", true) {
		override val prefix get() = PlsBundle.message("prefix.effect")
		override val descriptionText get() = PlsBundle.message("cwt.description.effect")
		override val icon get() = PlsIcons.Effect
	},
	Scope("scope", true) {
		override val prefix get() = PlsBundle.message("prefix.scope")
		override val descriptionText get() = PlsBundle.message("cwt.description.scope")
		override val icon get() = PlsIcons.Scope
	},
	ScopeGroup("scope group", true) {
		override val prefix get() = PlsBundle.message("prefix.scopeGroup")
		override val descriptionText get() = PlsBundle.message("cwt.description.scopeGroup")
		override val icon get() = PlsIcons.Scope
	},
	SystemLink("system link", true) {
		override val prefix get() = PlsBundle.message("prefix.systemLink")
		override val descriptionText get() = PlsBundle.message("cwt.description.systemLink")
		override val icon get() = PlsIcons.SystemScope
	},
	LocalisationLocale("localisation locale", true) {
		override val prefix get() = PlsBundle.message("prefix.localisationLocale")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationLocale")
		override val icon get() = PlsIcons.LocalisationLocale
	},
	LocalisationPredefinedParameter("localisation predefined parameter", true) {
		override val prefix get() = PlsBundle.message("prefix.localisationPredefinedParameter")
		override val descriptionText get() = PlsBundle.message("cwt.description.localisationPredefinedParameter")
		override val icon get() = PlsIcons.PredefinedParameter
	};
	
	abstract val prefix: String
	open val descriptionText: String? = null
	open val icon: Icon? = null
	
	fun getShortName(name: String) : String{
		//简单判断
		return when(this) {
			Type, Subtype, Enum, ComplexEnum, ValueSet -> name.substringIn('[',']')
			SingleAlias -> name.substringIn('[',']')
			Alias, Modifier, Trigger, Effect -> name.substringIn('[',']').substringAfter(':')
			else -> name
		}
	}
}
