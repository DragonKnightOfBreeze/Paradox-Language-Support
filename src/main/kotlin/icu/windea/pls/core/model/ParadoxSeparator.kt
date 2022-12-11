package icu.windea.pls.core.model;

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