@file:Suppress("unused")

package icu.windea.pls.core

import com.google.common.cache.*
import com.intellij.util.*
import com.intellij.util.containers.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import java.io.*
import java.net.*
import java.nio.charset.*
import java.nio.file.*
import java.util.*
import kotlin.contracts.*
import kotlin.math.*

@Suppress("NOTHING_TO_INLINE")
inline fun pass() {
}

@OptIn(ExperimentalContracts::class)
inline fun <T : R, R> T.letIf(condition: Boolean, block: (T) -> R): R {
	contract {
		callsInPlace(block, InvocationKind.AT_MOST_ONCE)
	}
	return if(condition) block(this) else this
}

@OptIn(ExperimentalContracts::class)
inline fun <T : R, R> T.letUnless(condition: Boolean, block: (T) -> R): R {
	contract {
		callsInPlace(block, InvocationKind.AT_MOST_ONCE)
	}
	return if(!condition) block(this) else this
}

fun Number.format(digits: Int): String {
	val power = 10.0.pow(abs(digits))
	return when {
		digits > 0 -> (round(this.toLong() / power) * power).toLong().toString()
		digits == 0 -> this.toLong().toString()
		else -> (round(this.toDouble() * power) / power).toString()
			.let { if(it.lastIndexOf('.') == -1) "$it.0" else it }
	}
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.takeIfNotEmpty() = this.takeIf { it.isNotEmpty() }

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

fun CharSequence.removePrefixOrNull(prefix: CharSequence, ignoreCase: Boolean = false): String? {
	return if(startsWith(prefix, ignoreCase)) substring(prefix.length) else null
}

fun String.removePrefixOrNull(prefix: CharSequence, ignoreCase: Boolean = false): String? {
	return if(startsWith(prefix, ignoreCase)) substring(prefix.length) else null
}

fun CharSequence.removeSuffixOrNull(suffix: CharSequence, ignoreCase: Boolean = false): String? {
	return if(endsWith(suffix, ignoreCase)) substring(0, length - suffix.length) else null
}

fun String.removeSuffixOrNull(suffix: CharSequence, ignoreCase: Boolean = false): String? {
	return if(endsWith(suffix, ignoreCase)) substring(0, length - suffix.length) else null
}

fun CharSequence.removeSurroundingOrNull(prefix: CharSequence, suffix: CharSequence, ignoreCase: Boolean = false): String? {
	return if(surroundsWith(prefix, suffix, ignoreCase)) substring(prefix.length, length - suffix.length) else null
}

fun String.removeSurroundingOrNull(prefix: CharSequence, suffix: CharSequence): String? {
	return if(surroundsWith(prefix, suffix)) substring(prefix.length, length - suffix.length) else null
}

/**
 * 根据指定的前后缀，得到首个符合条件的子字符串，如果找不到前缀或后缀，则返回默认值。
 * 默认值默认为当前字符串自身。
 */
@JvmOverloads
fun String.substringIn(prefix: Char, suffix: Char, missingDelimiterValue: String = this): String {
	val prefixIndex = indexOf(prefix).also { if(it == -1) return missingDelimiterValue }
	val suffixIndex = indexOf(suffix).also { if(it == -1) return missingDelimiterValue }
	return substring(prefixIndex + 1, suffixIndex)
}

/**
 * 根据指定的前后缀，得到首个符合条件的子字符串，如果找不到前缀或后缀，则返回默认值。
 * 默认值默认为当前字符串自身。
 */
@JvmOverloads
fun String.substringIn(prefix: String, suffix: String, missingDelimiterValue: String = this): String {
	val prefixIndex = indexOf(prefix).also { if(it == -1) return missingDelimiterValue }
	val suffixIndex = indexOf(suffix).also { if(it == -1) return missingDelimiterValue }
	return substring(prefixIndex + prefix.length, suffixIndex)
}

/**
 * 根据指定的前后缀，得到最后一个符合条件的子字符串，如果找不到前缀或后缀，则返回默认值。
 * 默认值默认为当前字符串自身。
 */
@JvmOverloads
fun String.substringInLast(prefix: Char, suffix: Char, missingDelimiterValue: String = this): String {
	val prefixIndex = lastIndexOf(prefix).also { if(it == -1) return missingDelimiterValue }
	val suffixIndex = lastIndexOf(suffix).also { if(it == -1) return missingDelimiterValue }
	return substring(prefixIndex + 1, suffixIndex)
}

/**
 * 根据指定的前后缀，得到最后一个符合条件的子字符串，如果找不到前缀或后缀，则返回默认值。
 * 默认值默认为当前字符串自身。
 */
@JvmOverloads
fun String.substringInLast(prefix: String, suffix: String, missingDelimiterValue: String = this): String {
	val prefixIndex = lastIndexOf(prefix).also { if(it == -1) return missingDelimiterValue }
	val suffixIndex = lastIndexOf(suffix).also { if(it == -1) return missingDelimiterValue }
	return substring(prefixIndex + prefix.length, suffixIndex)
}

private val blankRegex = "\\s+".toRegex()

fun String.splitByBlank(limit: Int = 0): List<String> {
	return split(blankRegex, limit)
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
	return false
}

fun Char.isExactLetter(): Boolean {
	return this in 'a'..'z' || this in 'A'..'Z'
}

fun Char.isExactDigit(): Boolean {
	return this in '0'..'9'
}

fun Char.isExactIdentifierChar(): Boolean {
	return this == '_' || this.isExactLetter() || this.isExactDigit()
}

fun String.isExactIdentifier(): Boolean {
	return this.all { it == '_' || it.isExactLetter() || it.isExactDigit() }
}

fun String.isLeftQuoted(): Boolean {
	return startsWith('"')
}

fun String.isRightQuoted(): Boolean {
	return length > 1 && endsWith('"') && get(length - 2) != '\\'
}

fun String.toCommaDelimitedStringList(): MutableList<String> {
	return this.splitToSequence(',').mapNotNullTo(SmartList()) { it.trim().takeIfNotEmpty() }
}

fun String.toCommaDelimitedStringSet(): MutableSet<String> {
	return this.splitToSequence(',').mapNotNullTo(mutableSetOf()) { it.trim().takeIfNotEmpty() }
}

fun Collection<String>.toCommaDelimitedString(): String {
	val input = this
	return if(input.isEmpty()) "" else input.joinToString(",")
}

fun String.quoteIf(condition: Boolean): String {
	return if(condition) quote() else this //不判断之前是否已经用引号括起，依据quoted 
}

fun String.quoteIfNecessary(): String {
	return if(containsBlank()) quote() else this //如果包含空白的话要使用引号括起
}

fun String.quote(): String {
	val s = this
	if(s.isEmpty() || s == "\"") return "\"\""
	val start = startsWith('"')
	val end = endsWith('"')
	if(start && end) return s
	return buildString {
		if(!start) append("\"")
		for(c in s) {
			if(c == '"') append("\\\"") else append(c)
		}
		if(!end) append("\"")
	}
}

fun String.unquote(): String {
	val s = this
	if(s.isEmpty() || s == "\"") return ""
	val start = startsWith('"')
	val end = endsWith('"')
	if(!start && !end) return s
	return buildString {
		var escape = false
		for((index, c) in s.withIndex()) {
			if(start && index == 0) continue
			if(end && index == s.lastIndex) continue
			if(escape) {
				escape = false
				when(c) {
					'\'' -> append(c)
					'\"' -> append(c)
					else -> append('\\').append(c)
				}
			} else if(c == '\\') {
				escape = true
			} else {
				append(c)
			}
		}
	}
}

fun String.truncate(limit: Int, ellipsis: String = "..."): String {
	return if(this.length <= limit) this else this.take(limit) + ellipsis
}

fun String.truncateAndKeepQuotes(limit: Int, ellipsis: String = "..."): String {
	if(this.isLeftQuoted()) {
		return if(this.length - 2 <= limit) this else this.take(limit + 1) + ellipsis + "\""
	} else {
		return if(this.length <= limit) this else this.take(limit) + ellipsis
	}
}

fun String.splitToPair(delimiter: Char): Pair<String, String>? {
	val index = this.indexOf(delimiter)
	if(index == -1) return null
	return this.substring(0, index) to this.substring(index + 1)
}

fun String.toCapitalizedWord(): String {
	if(isEmpty()) return this
	return this[0].uppercase() + this.substring(1)
}

fun String.toCapitalizedWords(): String {
	if(isEmpty()) return this
	return buildString {
		var isFirst = true
		var isWordStart = true
		for(c in this@toCapitalizedWords) {
			when {
				isWordStart -> {
					isWordStart = false
					if(isFirst) {
						isFirst = false
						append(c.uppercase())
					} else {
						append(c.lowercase())
					}
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

private val keywordDelimiters = charArrayOf('.', '_')

/**
 * 判断指定的关键词是否匹配当前字符串。
 */
fun String.matchesKeyword(keyword: String): Boolean {
	//IDEA低层如何匹配关键词：
	//com.intellij.codeInsight.completion.PrefixMatcher.prefixMatches(java.lang.String)
	
	//这里如何匹配关键词：按顺序包含所有字符，被跳过的子字符串必须以'.','_'结尾，忽略大小写
	if(keyword.isEmpty()) return true
	var index = -1
	var lastIndex = -2
	for(c in keyword) {
		index = indexOf(c, index + 1, true)
		when {
			index == -1 -> return false
			c !in keywordDelimiters && index != 0 && lastIndex != index - 1 && this[index - 1] !in keywordDelimiters -> return false
		}
		lastIndex = index
	}
	return true
}

fun CharSequence.indicesOf(char: Char, startIndex: Int = 0, ignoreCase: Boolean = false): List<Int> {
	var indices: MutableList<Int>? = null
	var lastIndex = indexOf(char, startIndex, ignoreCase)
	while(lastIndex != -1) {
		if(indices == null) indices = SmartList()
		indices.add(lastIndex)
		lastIndex = indexOf(char, lastIndex + 1, ignoreCase)
	}
	return indices ?: emptyList()
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

fun <C : CharSequence> C.ifNotEmpty(block: (C) -> C): C = if(this.isNotEmpty()) block(this) else this

fun String.matchesGlobFileName(pattern: String, ignoreCase: Boolean = false): Boolean {
	if(pattern.isEmpty()) return false
	if(pattern == "*") return true
	return pattern.split(';').any { doMatchGlobFileName(it.trim(), ignoreCase) }
}

private val globPatternToRegexCache = CacheBuilder.newBuilder().buildCache<String, Regex> {
	buildString {
		append("\\Q")
		var i = 0
		while(i < it.length) {
			val c = it[i]
			when {
				c == '*' -> append("\\E.*\\Q")
				c == '?' -> append("\\E.\\Q")
				else -> append(c)
			}
			i++
		}
		append("\\E")
	}.toRegex()
}

private fun String.doMatchGlobFileName(pattern: String, ignoreCase: Boolean): Boolean {
	val usedPath = this.let { if(ignoreCase) it.lowercase() else it }
	val usedPattern = pattern.let { if(ignoreCase) it.lowercase() else it }
	val regex = globPatternToRegexCache.get(usedPattern)
	return usedPath.matches(regex)
}

/**
 * 判断当前路径是否匹配另一个ANT路径通配符。使用"."匹配单个子路径中的单个字符，使用"*"匹配单个子路径中的任意个字符，使用"**"匹配任意个字符。
 */
fun String.matchesAntPath(pattern: String, ignoreCase: Boolean = false): Boolean {
	if(pattern.isEmpty()) return false
	if(pattern == "**" || pattern == "/**") return true
	return pattern.split(';').any { doMatchAntPath(it.trim(), ignoreCase) }
}

private val antPatternToRegexCache = CacheBuilder.newBuilder().buildCache<String, Regex> {
	buildString {
		append("\\Q")
		var i = 0
		while(i < it.length) {
			val c = it[i]
			when {
				c == '*' -> {
					val nc = it.getOrNull(i + 1)
					if(nc == '*') {
						i++
						append("\\E.*\\Q")
					} else {
						append("\\E[^/]*\\Q")
					}
				}
				c == '?' -> append("\\E[^/]\\Q")
				else -> append(c)
			}
			i++
		}
		append("\\E")
	}.toRegex()
}

private fun String.doMatchAntPath(pattern: String, ignoreCase: Boolean): Boolean {
	val usedPath = this.trim('/').let { if(ignoreCase) it.lowercase() else it }
	val usedPattern = pattern.trim('/').let { if(ignoreCase) it.lowercase() else it }
	val regex = antPatternToRegexCache.get(usedPattern)
	return usedPath.matches(regex)
}

/**
 * 判断当前路径是否匹配另一个路径（相同或者是其父路径）。使用"/"作为路径分隔符。
 * @param acceptSelf 是否接受路径完全一致的情况，默认为`true`。当使用文件路径匹配目录路径时，考虑设为`false`。
 * @param ignoreCase 是否忽略大小写。默认为`true`。
 */
fun String.matchesPath(other: String, acceptSelf: Boolean = true, ignoreCase: Boolean = true): Boolean {
	val path = if(ignoreCase) this.lowercase().trimEnd('/') else this
	val otherPath = if(ignoreCase) other.lowercase().trimEnd('/') else other
	if(path == otherPath) return acceptSelf
	if(path.length > otherPath.length) return false
	if(path == otherPath.take(length) && otherPath[length] == '/') return true
	return false
}

/**
 * 规范化当前路径。将路径分隔符统一替换成"/"，并去除所有作为前后缀的分隔符。
 */
fun String.normalizePath(): String {
	//目前仅当必要时才调用这个方法
	val builder = StringBuilder()
	var separatorFlag = false
	this.trim('/', '\\').forEach { c ->
		if(c == '/' || c == '\\') {
			separatorFlag = true
		} else if(separatorFlag) {
			separatorFlag = false
			builder.append('/').append(c)
		} else {
			builder.append(c)
		}
	}
	return builder.toString()
}

fun Path.exists(): Boolean {
	return Files.exists(this)
}

fun Path.notExists(): Boolean {
	return Files.notExists(this)
}

fun Path.create(): Path {
	if(isDirectory()) {
		createDirectories()
	} else {
		parent?.createDirectories()
		try {
			Files.createFile(this)
		} catch(e: FileAlreadyExistsException) {
			//ignored
		}
	}
	return this
}

fun Any?.toStringOrEmpty() = this?.toString() ?: ""

fun String.toBooleanYesNo() = this == "yes"

fun String.toBooleanYesNoOrNull() = if(this == "yes") true else if(this == "no") false else null

fun String.toUUID(): UUID {
	return UUID.nameUUIDFromBytes(toByteArray(StandardCharsets.UTF_8))
}

fun String.toUuidString(): String {
	return UUID.nameUUIDFromBytes(toByteArray(StandardCharsets.UTF_8)).toString()
}

fun String.toFile() = File(this)

fun String.toFileOrNull() = runCatching { File(this) }.getOrNull()

fun String.toPath() = Path.of(this)

fun String.toPathOrNull() = runCatching { Path.of(this) }.getOrNull()

/**
 * 得到当前文件绝对路径对应的URL。
 */
fun String.toFileUrl() = File(this).toURI().toURL()

/**
 * 得到当前相对于classpath的绝对路径对应的URL。
 */
fun String.toClasspathUrl() = PlsConstants.locationClass.getResource(this)!!

//fun String.toIntRangeOrNull(): IntRange? = runCatching { split("..", limit = 2).let { (a, b) -> a.toInt()..b.toInt() } }.getOrNull()
//
//fun String.toFloatRangeOrNull(): FloatRange? = runCatching { split("..", limit = 2).let { (a, b) -> a.toFloat()..b.toFloat() } }.getOrNull()

fun URL.toFile() = File(this.toURI())

fun URL.toPath() = Paths.get(this.toURI())

@PublishedApi
internal val enumValuesCache: LoadingCache<Class<*>, Array<*>> by lazy { CacheBuilder.newBuilder().buildCache { it.enumConstants } }

/**
 * 得到共享的指定枚举类型的所有枚举常量组成的数组。
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Enum<T>> sharedEnumValues(): Array<T> {
	return enumValuesCache[T::class.java] as Array<T>
}

@Suppress("UNCHECKED_CAST")
inline val <T : Enum<T>> Class<T>.sharedEnumConstants get() = enumValuesCache[this] as Array<T>

//fun <T> Result<T>.getOrThrow(predicate: (Throwable) -> Boolean): T? {
//	return this.onFailure { if(predicate(it)) throw it }.getOrNull()
//}
