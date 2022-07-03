package icu.windea.pls

import com.intellij.util.xmlb.*

class RegexIgnoreCaseConverter : Converter<Regex>() {
	override fun fromString(value: String): Regex {
		return value.toRegex(RegexOption.IGNORE_CASE)
	}
	
	override fun toString(t: Regex): String {
		return t.pattern
	}
}

class CommaDelimitedStringListConverter : Converter<List<String>>() {
	override fun fromString(value: String): List<String> {
		return value.toCommaDelimitedStringList()
	}
	
	override fun toString(value: List<String>): String {
		return value.toCommaDelimitedString()
	}
}

class CommaDelimitedStringSetIgnoreCaseConverter : Converter<Set<String>>() {
	override fun fromString(value: String): Set<String> {
		return value.toCommaDelimitedStringSet(ignoreCase = true)
	}
	
	override fun toString(value: Set<String>): String {
		return value.toCommaDelimitedString()
	}
}