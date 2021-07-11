package icu.windea.pls.cwt.config

enum class CwtConfigType(val text:String){
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
	ScopeGroup("scope group")
}