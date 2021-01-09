package com.windea.plugin.idea.paradox

import com.intellij.util.*
import com.windea.plugin.idea.paradox.*
import java.io.*
import java.net.*
import java.nio.file.*
import java.util.*
import javax.swing.*

fun Boolean.toInt() = if(this) 1 else 0

val workDirectory: File = File("").absoluteFile

private val classPathLocationClass = ParadoxBundle::class.java

private val defaultClassLoader = runCatching {
	classPathLocationClass.classLoader
}.recoverCatching {
	ClassLoader.getSystemClassLoader()
}.recoverCatching {
	ClassLoader.getPlatformClassLoader()
}.getOrThrow()

fun String.toClassPathResource(): URL? = defaultClassLoader.getResource(this)

@Suppress("UNCHECKED_CAST")
fun <T> Array<out T?>.cast() = this as Array<T>

inline fun <T, reified R> List<T>.mapArray(block: (T) -> R): Array<R> {
	return Array(size) { block(this[it]) }
}

inline fun <T, reified R> Array<out T>.mapArray(block: (T) -> R): Array<R> {
	return Array(size) { block(this[it]) }
}

inline fun <T, reified R> Sequence<T>.mapArray(block: (T) -> R): Array<R> {
	return this.toList().mapArray(block)
}

fun String.isBoolean() = this == "yes" || this == "no"

fun Boolean.toStringYesNo() = if(this) "yes" else "no"

fun String.toBooleanYesNo() = this == "yes"

fun String.isNumber(): Boolean {
	var isFirstChar = true
	var missingDot = true
	for(char in this.toCharArray()) {
		if(char.isDigit()) continue
		if(isFirstChar) {
			if(char == '+' || char == '-') continue
			isFirstChar = false
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

fun String.containsBlank() = this.any { it.isWhitespace() }

fun String.containsBlankLine(): Boolean {
	var newLine = 0
	val chars = toCharArray()
	for(i in chars.indices) {
		val char = chars[i]
		if((char == '\r' && chars[i + 1] != '\n') || char == '\n') newLine++
		if(newLine >= 2) return true
	}
	forEach {
		if(it == '\r' || it == '\n') newLine++
	}
	return false
}

fun String.quote() = if(startsWith('"') && endsWith('"')) this else "\"$this\""

fun String.quoteIfNecessary() = if(this.containsBlank()) quote() else this

fun String.onlyQuoteIfNecessary() = this.unquote().quoteIfNecessary()

fun String.unquote() = if(length >= 2 && startsWith('"') && endsWith('"')) substring(1, length - 1) else this

fun String.truncate(limit: Int) = if(this.length <= limit) this else this.take(limit) + "..."

fun String.toCapitalizedWord(): String {
	return if(isEmpty()) this else this[0].toUpperCase() + this.substring(1)
}

fun String.toCapitalizedWords(): String {
	return buildString {
		var isWordStart = true
		for(c in this@toCapitalizedWords.toCharArray()) {
			when {
				isWordStart -> {
					isWordStart = false
					append(c.toUpperCase())
				}
				c == '_' || c == '-' || c == '.' -> {
					isWordStart = true
					append('_')
				}
				else -> append(c)
			}
		}
	}
}

fun CharSequence.indicesOf(char: Char, ignoreCase: Boolean = false): MutableList<Int> {
	val indices = mutableListOf<Int>()
	var lastIndex = indexOf(char, 0, ignoreCase)
	while(lastIndex != -1) {
		indices += lastIndex
		lastIndex = indexOf(char, lastIndex + 1, ignoreCase)
	}
	return indices
}

inline fun <reified T> T.toSingletonArray(): Array<T> {
	return arrayOf(this)
}

inline fun <reified T> Sequence<T>.toArray(): Array<T> {
	return this.toList().toTypedArray()
}

fun <T> T.toSingletonList(): List<T> {
	return Collections.singletonList(this)
}

fun <T : Any> T?.toSingletonListOrEmpty(): List<T> {
	return if(this == null) Collections.emptyList() else Collections.singletonList(this)
}

fun Icon.resize(width: Int, height: Int): Icon {
	return IconUtil.toSize(this, width, height)
}

fun URL.toFile(): File {
	return File(this.toURI())
}

fun URL.toPath(): Path {
	return Path.of(this.toURI())
}

fun <C: CharSequence> C.ifNotEmpty(block: (C) -> Unit) {
	if(this.isNotEmpty()) block(this)
}

/**
 * 判断当前路径是否匹配另一个路径（是另一个路径的父路径）。
 */
infix fun String.matchesPath(other: String): Boolean {
	if(this == other) return true
	if(this == other.take(length) && other[length] == '/') return true
	return false
}

//Specific Models And Extensions

class ConditionalString(
	val expression: String
) : CharSequence{
	val name: String = expression.trimEnd('!', '?')
	val optional: Boolean = expression.endsWith('?')
	val required: Boolean = expression.endsWith('!')
	
	override val length = expression.length
	
	override fun get(index: Int): Char = expression[index]
	
	override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = expression.subSequence(startIndex, endIndex)
	
	override fun equals(other: Any?): Boolean = other is ConditionalString && expression == other.expression
	
	override fun hashCode(): Int = expression.hashCode()
	
	override fun toString(): String = expression
	
	operator fun component1(): String = name
	
	operator fun component2(): Boolean = optional
	
	operator fun component3(): Boolean = required
}

fun String.toConditionalKey() = ConditionalString(this)

class PredicateExpression(
	val expression:String
):CharSequence{
	//TODO
	
	override val length = expression.length
	
	override fun get(index: Int): Char = expression[index]
	
	override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = expression.subSequence(startIndex, endIndex)
	
	override fun equals(other: Any?): Boolean = other is PredicateExpression && expression == other.expression
	
	override fun hashCode(): Int = expression.hashCode()
	
	override fun toString(): String = expression
	
}