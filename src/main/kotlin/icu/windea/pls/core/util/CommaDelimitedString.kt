@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.core

import com.intellij.util.*
import com.intellij.util.containers.*
import icu.windea.pls.core.*
import kotlin.properties.*
import kotlin.reflect.*

@JvmInline
value class CommaDelimitedString(val value: String): Iterable<String> {
	fun contains(other: String, ignoreCase: Boolean = false): Boolean {
		val index = value.indexOf(other, ignoreCase = ignoreCase)
		if(index == -1) return false
		if(index != 0 && value[index - 1] != ',') return false
		val endIndex = index + other.length
		if(endIndex != value.length && value[endIndex + 1] != ',') return false
		return true
	}
	
	override fun iterator(): Iterator<String> {
		return value.splitToSequence(',').iterator()
	}
}

fun String?.asCommaDelimited(): CommaDelimitedString {
	return CommaDelimitedString(this.orEmpty())
}

fun String.toCommaDelimitedStringList(): MutableList<String> {
	return if(this.isEmpty()) {
		mutableListOf()
	} else {
		this.splitToSequence(',').mapNotNullTo(SmartList()) { it.trim().takeIfNotEmpty() }
	}
}

fun Collection<String>.toCommaDelimitedString(): String {
	val input = this
	return if(input.isEmpty()) "" else input.joinToString(",")
}