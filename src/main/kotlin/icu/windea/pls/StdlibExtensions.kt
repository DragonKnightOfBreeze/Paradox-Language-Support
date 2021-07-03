package icu.windea.pls

import com.intellij.util.*
import java.io.*
import java.net.*
import java.nio.file.*
import java.text.*
import java.util.*
import java.util.concurrent.*
import javax.swing.*

@Suppress("NOTHING_TO_INLINE")
inline fun pass() {
}

@Suppress("UNCHECKED_CAST")
fun <T> Array<out T?>.cast() = this as Array<T>

fun <T> Collection<T>.asList(): List<T> {
	return if(this is List) this else this.toList()
}

fun <T, E> List<T>.groupAndCountBy(selector: (T) -> E?): Map<E, Int> {
	val result = mutableMapOf<E, Int>()
	for(e in this) {
		val k = selector(e)
		if(k != null) {
			result.compute(k) { _, v -> if(v == null) 1 else v + 1 }
		}
	}
	return result
}

inline fun <T, reified R> List<T>.mapToArray(block: (T) -> R): Array<R> {
	return Array(size) { block(this[it]) }
}

inline fun <T, reified R> Array<out T>.mapToArray(block: (T) -> R): Array<R> {
	return Array(size) { block(this[it]) }
}

inline fun <T, reified R> Sequence<T>.mapToArray(block: (T) -> R): Array<R> {
	return toList().mapToArray(block)
}

fun CharSequence.surroundsWith(prefix: Char, suffix: Char, ignoreCase: Boolean = false): Boolean {
	return startsWith(prefix, ignoreCase) && endsWith(suffix, ignoreCase)
}

fun CharSequence.surroundsWith(prefix: CharSequence, suffix: CharSequence, ignoreCase: Boolean = false): Boolean {
	return endsWith(suffix, ignoreCase) && startsWith(prefix, ignoreCase) //先匹配后缀，这样可能会提高性能
}

fun CharSequence.removeSurrounding(prefix: CharSequence, suffix: CharSequence): CharSequence {
	return removePrefix(prefix).removeSuffix(suffix)
}

fun String.removeSurrounding(prefix: CharSequence, suffix: CharSequence): String {
	return removePrefix(prefix).removeSuffix(suffix)
}

fun String.resolveByRemovePrefix(prefix: CharSequence): String? {
	return if(startsWith(prefix)) substring(prefix.length) else null
}

fun String.resolveByRemoveSurrounding(prefix: CharSequence, suffix: CharSequence): String? {
	return if(surroundsWith(prefix, suffix)) substring(prefix.length, length - suffix.length) else null
}

fun String.containsBlank(): Boolean {
	return any { it.isWhitespace() }
}

fun String.containsLineBreak(): Boolean {
	return any { it == '\n' || it == '\r' }
}

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

fun String.quoteIf(quoted: Boolean) = if(quoted) "\"$this\"" else this //不判断之前是否已经用引号括起，依据quoted 

fun String.quoteIfNecessary() = if(containsBlank()) quote() else this //如果包含空白的话要使用引号括起

fun String.unquote() = if(length >= 2 && startsWith('"') && endsWith('"')) substring(1, length - 1) else this

fun String.truncate(limit: Int) = if(this.length <= limit) this else this.take(limit) + "..."

fun String.toCapitalizedWord(): String {
	return if(isEmpty()) this else this[0].uppercase() + this.substring(1)
}

fun String.toCapitalizedWords(): String {
	return buildString {
		var isWordStart = true
		for(c in this@toCapitalizedWords.toCharArray()) {
			when {
				isWordStart -> {
					isWordStart = false
					append(c.uppercase())
				}
				c == '_' || c == '-' || c == '.' -> {
					isWordStart = true
					append(' ')
				}
				else -> append(c.lowercase())
			}
		}
	}
}

private val keywordDelimiters = charArrayOf('.','_','-')

/**
 * 判断指定的关键词是否匹配当前字符串。（关键词中的每个字符是否按顺序被当前字符串包含，不区分大小写）。
 */
fun String.matchesKeyword(keyword: String,ignoreCase:Boolean = false): Boolean {
	//return contains(keyword)
	var index = -1
	for(c in keyword) {
		index = indexOf(c,index+1,ignoreCase)
		if(index == -1) return false
		else if(this[index-1] !in keywordDelimiters) return false
	}
	return true
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

fun <K, V> Map<K, V>.find(predicate: (Map.Entry<K, V>) -> Boolean): V? {
	for(entry in this) {
		if(predicate(entry)) return entry.value
	}
	throw NoSuchElementException()
}

fun <K, V> Map<K, V>.findOrNull(predicate: (Map.Entry<K, V>) -> Boolean): V? {
	for(entry in this) {
		if(predicate(entry)) return entry.value
	}
	return null
}

inline fun <reified T> Any?.cast(): T = this as T

inline fun <reified T> Any?.castOrNull(): T? = this as? T

fun Icon.resize(width: Int, height: Int = width): Icon {
	return IconUtil.toSize(this, width, height)
}

fun <C : CharSequence> C.ifNotEmpty(block: (C) -> Unit) {
	if(this.isNotEmpty()) block(this)
}

/**
 * 判断当前路径是否匹配另一个路径（等于或者是另一个路径的父路径）。
 */
infix fun String.matchesPath(other: String): Boolean {
	if(this == other) return true
	if(this == other.take(length) && other[length] == '/') return true
	return false
}

/**
 * 判断当前子路径列表是否宽松匹配另一个子路径列表（长度必须相等，当前子路径列表中的子路径可以是"any"，表示匹配任意子路径，忽略大小写）。
 */
infix fun List<String>.relaxMatchesPath(other: List<String>): Boolean {
	val size = size
	val otherSize = other.size
	if(size != otherSize) return false
	for(index in 0 until size) {
		val path = this[index].lowercase()
		if(path == "any") continue
		val otherPath = other[index].lowercase()
		if(path != otherPath) return false
	}
	return true
}

fun Path.exists(): Boolean {
	return Files.exists(this)
}

fun Path.notExists(): Boolean {
	return Files.notExists(this)
}

fun Path.tryCreateDirectory(): Any? {
	return try {
		Files.createDirectories(this)
	} catch(ignored: Exception) {
	}
}

val nullPair = null to null

@Suppress("UNCHECKED_CAST") 
fun <A,B> emptyPair() = nullPair as Pair<A,B>

fun <T> Collection<T>.toListOrThis(): List<T> {
	return when(this) {
		is List -> this
		else -> this.toList()
	}
}

//Is Extensions
fun String.isBooleanYesNo(): Boolean {
	return this == "yes" || this == "no"
}

fun String.isInt(): Boolean {
	var isFirstChar = true
	val chars = toCharArray()
	for(char in chars) {
		if(char.isDigit()) continue
		if(isFirstChar) {
			isFirstChar = false
			if(char == '+' || char == '-') continue
		}
		return false
	}
	return true
}

fun String.isFloat(): Boolean {
	var isFirstChar = true
	var missingDot = true
	val chars = toCharArray()
	for(char in chars) {
		if(char.isDigit()) continue
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

fun String.isString(): Boolean {
	//以引号包围，或者不是布尔值、整数以及小数
	if(surroundsWith('"', '"')) return true
	return !isBooleanYesNo() && !isInt() && !isFloat()
}

fun String.isPercentageField(): Boolean {
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

private val isColorRegex = """(rgb|rgba|hsb|hsv|hsl)[ \u00a0\t]*\{[0-9. \u00a0\t]*}""".toRegex()

fun String.isColorField(): Boolean {
	return this.matches(isColorRegex)
}

private val threadLocalDateFormat = ThreadLocal.withInitial { SimpleDateFormat("yyyy.MM.dd") }

fun String.isDateField(): Boolean {
	return try {
		threadLocalDateFormat.get().parse(this)
		true
	} catch(e: Exception) {
		false
	}
}

fun String.isVariableField(): Boolean {
	return this.startsWith('@') //NOTE 简单判断
}

fun String.isTypeOf(type: String): Boolean {
	return (type == "boolean" && isBooleanYesNo()) || (type == "int" && isInt()) || (type == "float" && isFloat())
		|| (type == "color" && isColorField()) || type == "string"
}

//To Extensions

fun Any?.toStringOrEmpty() = this?.toString() ?: ""

fun Boolean.toInt() = if(this) 1 else 0

fun Boolean.toStringYesNo() = if(this) "yes" else "no"

fun String.toBooleanYesNo() = this == "yes"

fun String.toBooleanYesNoOrNull() = if(this == "yes") true else if(this == "no") false else null

fun String.toUrl(locationClass: Class<*>) = locationClass.getResource(this)!!

fun String.toIntRangeOrNull() = runCatching { split("..", limit = 2).let { (a, b) -> a.toInt()..b.toInt() } }.getOrNull()

fun String.toFloatRangeOrNull() = runCatching { split("..", limit = 2).let { (a, b) -> a.toFloat()..b.toFloat() } }.getOrNull()

fun URL.toFile() = File(this.toURI())

fun URL.toPath() = Paths.get(this.toURI())

inline fun <reified T> T.toSingletonArray() = arrayOf(this)

inline fun <reified T> Sequence<T>.toArray() = this.toList().toTypedArray()

fun <T> T.toSingletonList() = Collections.singletonList(this)

fun <T : Any> T?.toSingletonListOrEmpty() = if(this == null) Collections.emptyList() else Collections.singletonList(this)

//System Extensions

/**
 * 执行命令。（基于操作系统）
 */
fun exec(command: String, workDirectory: File? = null): Process {
	return Runtime.getRuntime().exec(optimizeCommand(command), null, workDirectory)
}

/**
 * 执行命令并阻塞进程到执行结束。（基于操作系统）
 */
fun execBlocking(command: String, workDirectory: File? = null): Process {
	return Runtime.getRuntime().exec(optimizeCommand(command), null, workDirectory).apply { waitFor() }
}

/**
 * 执行命令并阻塞进程到执行结束。（基于操作系统）
 */
fun execBlocking(command: String, timeout: Long, timeUnit: TimeUnit, workDirectory: File? = null): Process {
	return Runtime.getRuntime().exec(optimizeCommand(command), null, workDirectory).apply { waitFor(timeout, timeUnit) }
}

private fun optimizeCommand(command: String): Array<String> {
	return arrayOf("cmd", "/c", command)
}

//Specific Collections

data class ReversibleList<T>(val list: List<T>, val reverse: Boolean) : List<T> by list

fun <T> List<T>.toReversibleList(reverse: Boolean) = ReversibleList(this, reverse)

data class ReversibleMap<K, V>(val map: Map<K, V>, val reverse: Boolean = false) : Map<K, V> by map

fun <K, V> Map<K, V>.toReversibleMap(reverse: Boolean) = ReversibleMap(this, reverse)

interface Enumerable {
	val key: String
	val text: String
}

//Tuple Extensions

typealias Tuple2<A, B> = Pair<A, B>

typealias Tuple3<A, B, C> = Triple<A, B, C>

fun <A, B> tupleOf(first: A, second: B) = Tuple2(first, second)

fun <A, B, C> tupleOf(first: A, second: B, third: C) = Tuple3(first, second, third)

//Range Extensions

typealias FloatRange = ClosedRange<Float>