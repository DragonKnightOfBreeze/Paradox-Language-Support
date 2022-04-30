package icu.windea.pls.config.cwt.config

import icu.windea.pls.*

enum class CwtConfigType(
	override val text: String
) : TextAware {
	Type("type"),
	Subtype("subtype"),
	Enum("enum"),
	ComplexEnum("complex enum"),
	Value("value"),
	SingleAlias("single alias"),
	Alias("alias"),
	
	EnumValue("enum value"),
	ValueValue("value value"),
	Link("link"),
	LocalisationLink("localisation link"),
	LocalisationCommand("localisation command"),
	ModifierCategory("modifier category"),
	Modifier("modifier"),
	Scope("scope"),
	ScopeGroup("scope group");
	
	override fun toString(): String {
		return text
	}
}