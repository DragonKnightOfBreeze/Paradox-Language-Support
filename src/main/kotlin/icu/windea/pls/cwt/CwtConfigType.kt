package icu.windea.pls.cwt

import icu.windea.pls.*

enum class CwtConfigType(
	override val id: String,
	override val text: String
) : IdAware, TextAware {
	Type("type", PlsDocBundle.message("name.cwt.type")),
	Subtype("subtype", PlsDocBundle.message("name.cwt.subtype")),
	Enum("enum", PlsDocBundle.message("name.cwt.enum")),
	ComplexEnum("complex enum", PlsDocBundle.message("name.cwt.complexEnum")),
	Value("value", PlsDocBundle.message("name.cwt.value")),
	SingleAlias("single alias", PlsDocBundle.message("name.cwt.singleAlias")),
	Alias("alias", PlsDocBundle.message("name.cwt.alias")),
	
	EnumValue("enum value", PlsDocBundle.message("name.cwt.enumValue")),
	ValueValue("value value", PlsDocBundle.message("name.cwt.valueValue")),
	Link("link", PlsDocBundle.message("name.cwt.link")),
	LocalisationLink("localisation link", PlsDocBundle.message("name.cwt.localisationLink")),
	LocalisationCommand("localisation command", PlsDocBundle.message("name.cwt.localisationCommand")),
	ModifierCategory("modifier category", PlsDocBundle.message("name.cwt.modifierCategory")),
	Modifier("modifier", PlsDocBundle.message("name.cwt.modifier")),
	Scope("scope", PlsDocBundle.message("name.cwt.scope")),
	ScopeGroup("scope group", PlsDocBundle.message("name.cwt.scopeGroup")),
	
	LocalisationLocale("localisation locale", PlsDocBundle.message("name.cwt.localisationLocale")),
	LocalisationSequentialNumber("localisation sequential number", PlsDocBundle.message("name.cwt.localisationSequentialNumber")),
	LocalisationColor("localisation color", PlsDocBundle.message("name.cwt.localisationColor"));
	
	
	
	override fun toString(): String {
		return text
	}
}