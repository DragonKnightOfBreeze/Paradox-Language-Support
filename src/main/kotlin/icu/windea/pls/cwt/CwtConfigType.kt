package icu.windea.pls.cwt

import icu.windea.pls.*

enum class CwtConfigType(
	override val id: String,
	override val text: String,
	val isReference: Boolean
) : IdAware, TextAware {
	Type("type", PlsDocBundle.message("name.cwt.type"), false),
	Subtype("subtype", PlsDocBundle.message("name.cwt.subtype"), false),
	Enum("enum", PlsDocBundle.message("name.cwt.enum"), false),
	ComplexEnum("complex enum", PlsDocBundle.message("name.cwt.complexEnum"), false),
	Value("value", PlsDocBundle.message("name.cwt.value"), false),
	SingleAlias("single alias", PlsDocBundle.message("name.cwt.singleAlias"), false),
	Alias("alias", PlsDocBundle.message("name.cwt.alias"), false),
	
	EnumValue("enum value", PlsDocBundle.message("name.cwt.enumValue"), true),
	ValueValue("value value", PlsDocBundle.message("name.cwt.valueValue"), true),
	Link("link", PlsDocBundle.message("name.cwt.link"), true),
	LocalisationLink("localisation link", PlsDocBundle.message("name.cwt.localisationLink"), true),
	LocalisationCommand("localisation command", PlsDocBundle.message("name.cwt.localisationCommand"), true),
	ModifierCategory("modifier category", PlsDocBundle.message("name.cwt.modifierCategory"), true),
	Modifier("modifier", PlsDocBundle.message("name.cwt.modifier"), true),
	Scope("scope", PlsDocBundle.message("name.cwt.scope"), true),
	ScopeGroup("scope group", PlsDocBundle.message("name.cwt.scopeGroup"), true),
	Tag("tag", PlsDocBundle.message("name.cwt.tag"), true),
	
	LocalisationLocale("localisation locale", PlsDocBundle.message("name.cwt.localisationLocale"), true),
	LocalisationColor("localisation color", PlsDocBundle.message("name.cwt.localisationColor"), true),
	LocalisationPredefinedVariable("localisation predefined variable", PlsDocBundle.message("name.cwt.localisationPredefinedVariable"), true);
	
	override fun toString(): String {
		return text
	}
}