@file:Suppress("unused")

package icu.windea.pls

import com.google.common.cache.*
import com.intellij.util.*
import com.intellij.util.containers.*
import com.intellij.util.io.*
import org.jetbrains.kotlin.util.capitalizeDecapitalize.*
import java.io.*
import java.net.*
import java.nio.charset.*
import java.nio.file.*
import java.util.*
import kotlin.math.*

//region Common Extensions
@Suppress("NOTHING_TO_INLINE")
inline fun pass() {}

fun Number.format(digits: Int): String {
	val power = 10.0.pow(abs(digits))
	return when {
		digits > 0 -> (round(this.toLong() / power) * power).toLong().toString()
		digits == 0 -> this.toLong().toString()
		else -> (round(this.toDouble() * power) / power).toString()
			.let { if(it.lastIndexOf('.') == -1) "$it.0" else it }
	}
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

fun Char.isExactDigit(): Boolean {
	return this in '0'..'9'
}

fun Char.isExactLetter(): Boolean {
	return this in 'a'..'z' || this in 'A'..'Z'
}

fun String.isQuoted() = length >= 2 && surroundsWith('"', '"')

fun String.quote() = if(isQuoted()) this else "\"$this\""

fun String.quoteIf(quoted: Boolean) = if(quoted) "\"$this\"" else this //不判断之前是否已经用引号括起，依据quoted 

fun String.quoteIfNecessary() = if(containsBlank()) quote() else this //如果包含空白的话要使用引号括起

fun String.unquote() = if(isQuoted()) substring(1, length - 1) else this

fun String.truncate(limit: Int, ellipsis: String = "..."): String {
	return if(this.length <= limit) this else this.take(limit) + ellipsis
}

fun String.truncateAndKeepQuotes(limit: Int, ellipsis: String = "..."): String {
	if(this.isQuoted()) {
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
	return if(isEmpty()) this else this[0].uppercase() + this.substring(1)
}

fun String.toCapitalizedWords(): String {
	return buildString {
		var isWordStart = true
		for(c in this@toCapitalizedWords) {
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

private val keywordDelimiters = charArrayOf('.', '_')

/**
 * 判断指定的关键词是否匹配当前字符串。
 */
fun String.matchesKeyword(keyword: String): Boolean {
	//IDEA低层如何匹配关键词：
	//com.intellij.codeInsight.completion.PrefixMatcher.prefixMatches(java.lang.String)
	//这里如何匹配关键词：包含，忽略大小写
	return keyword.isEmpty() || contains(keyword, true)
	
	////这里如何匹配关键词：部分包含，被跳过的子字符串必须以'.','_'结尾，忽略大小写
	//if(keyword.isEmpty()) return true
	//var index = -1
	//var lastIndex = -2
	//for(c in keyword) {
	//	index = indexOf(c,index+1,ignoreCase)
	//	when {
	//		index == -1 -> return false
	//		c !in keywordDelimiters && index != 0 && lastIndex != index-1 
	//		    && this[index-1] !in keywordDelimiters -> return false
	//	}
	//	lastIndex = index
	//}
	//return true
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

fun <C : CharSequence> C.ifNotEmpty(block: (C) -> C): C = if(this.isNotEmpty()) block(this) else this

fun String.toCommaDelimitedStringMutableList(): MutableList<String> {
	val input = this.trim()
	return if(input.isEmpty()) {
		mutableListOf()
	} else {
		input.splitToSequence(',').mapNotNullTo(SmartList()) { it.trim().takeIf { s -> s.isNotEmpty() } }
	}
}

fun String.toCommaDelimitedStringList(): List<String> {
	val input = this.trim()
	return if(input.isEmpty()) {
		emptyList()
	} else {
		input.splitToSequence(',').mapNotNullTo(SmartList()) { it.trim().takeIf { s -> s.isNotEmpty() } }
	}
}

fun String.toCommaDelimitedStringSet(ignoreCase: Boolean = false): Set<String> {
	val input = this.trim()
	return if(input.isEmpty()) {
		emptySet()
	} else {
		val set = if(ignoreCase) CollectionFactory.createCaseInsensitiveStringSet() else mutableSetOf()
		input.splitToSequence(',').mapNotNullTo(set) { it.trim().takeIf { s -> s.isNotEmpty() } }
	}
}

fun Collection<String>.toCommaDelimitedString(): String {
	val input = this
	return if(input.isEmpty()) "" else input.joinToString(",")
}

/**
 * 判断当前路径是否匹配另一个ANT路径通配符。使用"."匹配单个字符，使用"*"匹配单个子路径中的任意个字符，使用"**"匹配任意个字符。
 */
fun String.matchesAntPath(pattern: String, ignoreCase: Boolean = false): Boolean {
	val usedPath = this.trimEnd('/')
	val usedPattern = pattern.trimEnd('/')
	var flag = false
	val patternLength = usedPattern.length
	var patternIndex = 0
	val pathLength = usedPath.length
	var pathIndex = 0
	while(patternIndex < patternLength) {
		val c = pattern[patternIndex]
		when {
			flag -> {
				patternIndex++
				val nc = pattern.getOrNull(patternIndex)
				if(nc == '*') {
					patternIndex++
					val nextPatternChar = usedPattern.getOrNull(patternIndex) ?: return true
					while(true) {
						if(pathIndex >= pathLength) return true
						val c1 = usedPath[pathIndex]
						if(c1.equals(nextPatternChar, ignoreCase)) break
						pathIndex++
					}
					flag = false
				} else {
					val nextPatternChar = nc
					while(true) {
						if(pathIndex >= pathLength) return true
						val c1 = usedPath[pathIndex]
						if(c1 == '/') break
						if(nextPatternChar != null && c1.equals(nextPatternChar, ignoreCase)) break
						pathIndex++
					}
					flag = false
					continue
				}
			}
			c == '.' -> {
				if(pathIndex == pathLength - 1) return true
				pathIndex++
				if(pathIndex > pathLength) return false
				patternIndex++
			}
			c == '*' -> {
				flag = true
			}
			else -> {
				val cc = usedPath[pathIndex]
				if(!cc.equals(c, ignoreCase)) return false
				if(pathIndex == pathLength - 1) return true
				pathIndex++
				if(pathIndex > pathLength) return false
				patternIndex++
			}
		}
	}
	return flag
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
 * 判断当前子路径列表是否完全匹配另一个子路径列表（相同）。使用"/"作为路径分隔符。
 * @param ignoreCase 是否忽略大小写。默认为`true`。
 * @param useAnyWildcard 是否使用`"any"`字符串作为子路径通配符。表示匹配任意子路径。默认为`true`。
 * @param matchesParent 是否需要仅匹配另一个路径的父路径。默认为`false`。
 */
fun List<String>.matchEntirePath(other: List<String>, ignoreCase: Boolean = true, useAnyWildcard: Boolean = true, matchesParent: Boolean = false): Boolean {
	val size = size
	val otherSize = if(matchesParent) other.size - 1 else other.size
	if(size != otherSize) return false //路径长度不一致
	if(otherSize < 0) return false //需要匹配另一个路径的父路径而另一个路径为空
	for(index in 0 until size) {
		val path = if(ignoreCase) this[index].lowercase() else this[index]
		if(useAnyWildcard && path == "any") continue
		val otherPath = if(ignoreCase) other[index].lowercase() else other[index]
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

val nullPair = null to null

@Suppress("UNCHECKED_CAST")
fun <A, B> emptyPair() = nullPair as Pair<A, B>

fun <T> Collection<T>.toListOrThis(): List<T> {
	return when(this) {
		is List -> this
		else -> this.toList()
	}
}

fun Any?.toStringOrEmpty() = this?.toString() ?: ""

fun Boolean.toInt() = if(this) 1 else 0

fun Boolean.toStringYesNo() = if(this) "yes" else "no"

fun String.toBooleanYesNo() = this == "yes"

fun String.toBooleanYesNoOrNull() = if(this == "yes") true else if(this == "no") false else null

fun String.toIntOrDefault(defaultValue: Int) = runCatching { toInt() }.getOrDefault(defaultValue)

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
fun String.toClasspathUrl() = locationClass.getResource(this)!!

fun String.toIntRangeOrNull(): IntRange? = runCatching { split("..", limit = 2).let { (a, b) -> a.toInt()..b.toInt() } }.getOrNull()

fun String.toFloatRangeOrNull(): FloatRange? = runCatching { split("..", limit = 2).let { (a, b) -> a.toFloat()..b.toFloat() } }.getOrNull()

fun String.toUUID(): UUID {
	return UUID.nameUUIDFromBytes(toByteArray(StandardCharsets.UTF_8))
}

fun URL.toFile() = File(this.toURI())

fun URL.toPath() = Paths.get(this.toURI())

@PublishedApi
internal val enumValuesCache: LoadingCache<Class<*>, Array<*>> by lazy { createCache { it.enumConstants } }

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
//endregion

//region Collection Extensions
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

inline fun <T, R : Any, C : MutableCollection<in R>> Iterable<T>.mapNotNullTo(destination: C, transform: (T) -> R?): C {
	for(item in this) {
		val result = transform(item)
		if(result != null) destination.add(result)
	}
	return destination
}

inline fun <T, R : Any> List<T>.mapAndFirst(predicate: (R?) -> Boolean = { it != null }, transform: (T) -> R?): R? {
	if(this.isEmpty()) return null
	var first: R? = null
	for(element in this) {
		val result = transform(element)
		if(predicate(result)) return result
		first = result
	}
	return first
}

inline fun <T, reified R> Array<out T>.mapToArray(transform: (T) -> R): Array<R> {
	return Array(size) { transform(this[it]) }
}

inline fun <T, reified R> List<T>.mapToArray(transform: (T) -> R): Array<R> {
	return Array(size) { transform(this[it]) }
}

inline fun <T, reified R> Set<T>.mapToArray(transform: (T) -> R): Array<R> {
	val result = arrayOfNulls<R>(this.size)
	for((i, e) in this.withIndex()) {
		result[i] = transform(e)
	}
	return result.cast()
}

inline fun <K, V, reified R> Map<K, V>.mapToArray(transform: (Map.Entry<K, V>) -> R): Array<R> {
	//这里不先将Set转为List
	val size = this.size
	val entries = this.entries
	try {
		val iterator = entries.iterator()
		return Array(size) { transform(iterator.next()) }
	} catch(e: Exception) {
		val list = entries.toList()
		return Array(size) { transform(list[it]) }
	}
}

inline fun <T, reified R> Sequence<T>.mapToArray(transform: (T) -> R): Array<R> {
	return toList().mapToArray(transform)
}

fun <K, V> mapOfKv(key: K, value: V): Map<K, V> {
	return Collections.singletonMap(key, value)
}

fun <T> Collection<T>.parallelForEach(action: (T) -> Unit) {
	return this.parallelStream().forEach(action)
}

fun <K, V> Map<K, V>.parallelForEach(action: (Map.Entry<K, V>) -> Unit) {
	return this.entries.parallelStream().forEach(action)
}

inline fun <reified T> T.toSingletonArray() = arrayOf(this)

inline fun <reified T> Sequence<T>.toArray() = this.toList().toTypedArray()

fun <T> T.toSingletonList(): List<T> = Collections.singletonList(this)

fun <T : Any> T?.toSingletonListOrEmpty(): List<T> = if(this == null) Collections.emptyList() else Collections.singletonList(this)

data class ReversibleList<T>(val list: List<T>, val notReversed: Boolean) : List<T> by list {
	override fun contains(element: T): Boolean {
		return if(notReversed) list.contains(element) else !list.contains(element)
	}
	
	override fun containsAll(elements: Collection<T>): Boolean {
		return if(notReversed) list.containsAll(elements) else !list.containsAll(elements)
	}
}

fun <T> List<T>.toReversibleList(notReversed: Boolean) = ReversibleList(this, notReversed)

data class ReversibleSet<T>(val set: Set<T>, val notReversed: Boolean) : Set<T> by set {
	override fun contains(element: T): Boolean {
		return if(notReversed) set.contains(element) else !set.contains(element)
	}
	
	override fun containsAll(elements: Collection<T>): Boolean {
		return if(notReversed) set.containsAll(elements) else !set.containsAll(elements)
	}
}

fun <T> Set<T>.toReversibleSet(notReversed: Boolean) = ReversibleSet(this, notReversed)

data class ReversibleMap<K, V>(val map: Map<K, V>, val notReversed: Boolean = false) : Map<K, V> by map {
	override fun containsKey(key: K): Boolean {
		return if(notReversed) map.containsKey(key) else !map.containsKey(key)
	}
	
	override fun containsValue(value: V): Boolean {
		return if(notReversed) map.containsValue(value) else !map.containsValue(value)
	}
}

fun <K, V> Map<K, V>.toReversibleMap(notReversed: Boolean) = ReversibleMap(this, notReversed)

fun <T> Iterable<T>.process(processor: ProcessEntry.(T) -> Boolean): Boolean {
	for(e in this) {
		val result = ProcessEntry.processor(e)
		if(!result) return false
	}
	return true
}

fun <K, V> Map<K, V>.process(processor: ProcessEntry.(Map.Entry<K, V>) -> Boolean): Boolean {
	for(entry in this) {
		val result = ProcessEntry.processor(entry)
		if(!result) return false
	}
	return true
}

object ProcessEntry {
	fun <T> T.addTo(destination: MutableCollection<T>) = destination.add(this)
	
	fun <T : Collection<T>> T.addAllTo(destination: MutableCollection<T>) = destination.addAll(this)
	
	fun Any?.end() = true
	
	fun Any?.stop() = false
}
//endregion

//region Tuple & Range Extensions
typealias Tuple2<A, B> = Pair<A, B>

typealias TypedTuple2<T> = Pair<T, T>

typealias Tuple3<A, B, C> = Triple<A, B, C>

typealias TypedTuple3<T> = Triple<T, T, T>

fun <A, B> tupleOf(first: A, second: B) = Tuple2(first, second)

fun <A, B, C> tupleOf(first: A, second: B, third: C) = Tuple3(first, second, third)

typealias FloatRange = ClosedRange<Float>

public inline operator fun FloatRange.contains(element: Float?): Boolean {
	return element != null && contains(element)
}
//endregion

//region Other Types
data class BlockEntry<out K, out V>(
	val key: K,
	val value: V
)
//endregion