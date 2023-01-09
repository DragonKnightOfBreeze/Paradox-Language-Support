package icu.windea.pls.config.core.config;

enum class ParadoxSeparator(val text :String) {
	EQUAL("="),
	NOT_EQUAL("!="),
	LT("<"),
	GT(">"),
	LE("<="),
	GE(">=");
	
	override fun toString(): String {
		return text
	}
}