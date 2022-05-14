package icu.windea.pls.config.cwt

import icu.windea.pls.*
import java.text.*

//region String Predicate Extensions
internal fun String.isInt(): Boolean {
	var isFirstChar = true
	val chars = toCharArray()
	for(char in chars) {
		if(char.isExactDigit()) continue
		if(isFirstChar) {
			isFirstChar = false
			if(char == '+' || char == '-') continue
		}
		return false
	}
	return true
}

internal fun String.isFloat(): Boolean {
	var isFirstChar = true
	var missingDot = true
	val chars = toCharArray()
	for(char in chars) {
		if(char.isExactDigit()) continue
		if(isFirstChar) {
			isFirstChar = false
			if(char == '+' || char == '-') continue
		}
		if(missingDot) {
			if(char == '.') {
				missingDot = false
				continue
			}
		}
		return false
	}
	return true
}

internal fun String.isBooleanYesNo(): Boolean {
	return this == "yes" || this == "no"
}

internal fun String.isString(): Boolean {
	//以引号包围，或者不是布尔值、整数以及小数
	if(surroundsWith('"', '"')) return true
	return !isBooleanYesNo() && !isInt() && !isFloat()
}

internal fun String.isPercentageField(): Boolean {
	val chars = toCharArray()
	for(i in indices) {
		val char = chars[i]
		if(i == lastIndex) {
			if(char != '%') return false
		} else {
			if(!char.isDigit()) return false
		}
	}
	return true
}

private val isColorRegex = """(?:rgb|rgba|hsb|hsv|hsl)[ \t]*\{[\d. \t]*}""".toRegex()

internal fun String.isColorField(): Boolean {
	return this.matches(isColorRegex)
}

private val threadLocalDateFormat = ThreadLocal.withInitial { SimpleDateFormat("yyyy.MM.dd") }

internal fun String.isDateField(): Boolean {
	return try {
		threadLocalDateFormat.get().parse(this)
		true
	} catch(e: Exception) {
		false
	}
}

internal fun String.isVariableField(): Boolean {
	return this.startsWith('@') //NOTE 简单判断
}
//endregion