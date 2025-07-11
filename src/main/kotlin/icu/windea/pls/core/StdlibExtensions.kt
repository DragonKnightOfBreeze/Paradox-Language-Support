@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core

import com.google.common.cache.*
import com.intellij.openapi.util.text.*
import icu.windea.pls.*
import icu.windea.pls.core.console.*
import icu.windea.pls.core.util.*
import java.io.*
import java.net.*
import java.nio.charset.*
import java.nio.file.*
import java.util.*
import kotlin.contracts.*
import kotlin.io.path.*
import kotlin.math.*

private data object EmptyObject

val EMPTY_OBJECT: Any = EmptyObject

inline fun pass() {}

@OptIn(ExperimentalContracts::class)
inline fun <T : R, R> T.letIf(condition: Boolean, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (condition) block(this) else this
}

@OptIn(ExperimentalContracts::class)
inline fun <T : R, R> T.letUnless(condition: Boolean, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (!condition) block(this) else this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> T.alsoIf(condition: Boolean, block: (T) -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    if (condition) block(this)
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> T.alsoUnless(condition: Boolean, block: (T) -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    if (!condition) block(this)
    return this
}

@OptIn(ExperimentalContracts::class)
@Suppress("ReplaceSizeCheckWithIsNotEmpty")
inline fun CharSequence?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }

    return this != null && this.length != 0
}

@OptIn(ExperimentalContracts::class)
@Suppress("ReplaceSizeCheckWithIsNotEmpty")
inline fun Array<*>?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }

    return this != null && this.size != 0
}

@OptIn(ExperimentalContracts::class)
@Suppress("ReplaceSizeCheckWithIsNotEmpty")
inline fun Collection<*>?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }

    return this != null && this.size != 0
}

fun Number.format(digits: Int): String {
    val power = 10.0.pow(abs(digits))
    return when {
        digits > 0 -> (round(this.toLong() / power) * power).toLong().toString()
        digits == 0 -> this.toLong().toString()
        else -> (round(this.toDouble() * power) / power).toString()
            .let { if (it.lastIndexOf('.') == -1) "$it.0" else it }
    }
}

inline fun <T : CharSequence> T.orNull() = this.takeIf { it.isNotEmpty() }

fun CharSequence.surroundsWith(prefix: Char, suffix: Char, ignoreCase: Boolean = false): Boolean {
    return startsWith(prefix, ignoreCase) && endsWith(suffix, ignoreCase)
}

fun CharSequence.surroundsWith(prefix: CharSequence, suffix: CharSequence, ignoreCase: Boolean = false): Boolean {
    return endsWith(suffix, ignoreCase) && startsWith(prefix, ignoreCase) //先匹配后缀，这样可能会提高性能
}

fun CharSequence.addPrefix(prefix: CharSequence): String {
    return prefix.toString() + this.toString()
}

fun CharSequence.addSuffix(suffix: CharSequence): String {
    return this.toString() + suffix.toString()
}

fun CharSequence.addSurrounding(prefix: CharSequence, suffix: CharSequence): String {
    return prefix.toString() + this.toString() + suffix.toString()
}

fun CharSequence.removeSurrounding(prefix: CharSequence, suffix: CharSequence): CharSequence {
    return removePrefix(prefix).removeSuffix(suffix)
}

fun String.removeSurrounding(prefix: CharSequence, suffix: CharSequence): String {
    return removePrefix(prefix).removeSuffix(suffix)
}

fun CharSequence.removePrefixOrNull(prefix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (startsWith(prefix, ignoreCase)) substring(prefix.length) else null
}

fun String.removePrefixOrNull(prefix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (startsWith(prefix, ignoreCase)) substring(prefix.length) else null
}

fun CharSequence.removeSuffixOrNull(suffix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (endsWith(suffix, ignoreCase)) substring(0, length - suffix.length) else null
}

fun String.removeSuffixOrNull(suffix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (endsWith(suffix, ignoreCase)) substring(0, length - suffix.length) else null
}

fun CharSequence.removeSurroundingOrNull(prefix: CharSequence, suffix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (surroundsWith(prefix, suffix, ignoreCase)) substring(prefix.length, length - suffix.length) else null
}

fun String.removeSurroundingOrNull(prefix: CharSequence, suffix: CharSequence): String? {
    return if (surroundsWith(prefix, suffix)) substring(prefix.length, length - suffix.length) else null
}

/**
 * 根据指定的前后缀，得到首个符合条件的子字符串，如果找不到前缀或后缀，则返回默认值。
 * 默认值默认为当前字符串自身。
 */
@JvmOverloads
fun String.substringIn(prefix: Char, suffix: Char, missingDelimiterValue: String = this): String {
    val prefixIndex = indexOf(prefix).also { if (it == -1) return missingDelimiterValue }
    val suffixIndex = indexOf(suffix).also { if (it == -1) return missingDelimiterValue }
    return substring(prefixIndex + 1, suffixIndex)
}

/**
 * 根据指定的前后缀，得到首个符合条件的子字符串，如果找不到前缀或后缀，则返回默认值。
 * 默认值默认为当前字符串自身。
 */
@JvmOverloads
fun String.substringIn(prefix: String, suffix: String, missingDelimiterValue: String = this): String {
    val prefixIndex = indexOf(prefix).also { if (it == -1) return missingDelimiterValue }
    val suffixIndex = indexOf(suffix).also { if (it == -1) return missingDelimiterValue }
    return substring(prefixIndex + prefix.length, suffixIndex)
}

/**
 * 根据指定的前后缀，得到最后一个符合条件的子字符串，如果找不到前缀或后缀，则返回默认值。
 * 默认值默认为当前字符串自身。
 */
@JvmOverloads
fun String.substringInLast(prefix: Char, suffix: Char, missingDelimiterValue: String = this): String {
    val prefixIndex = lastIndexOf(prefix).also { if (it == -1) return missingDelimiterValue }
    val suffixIndex = lastIndexOf(suffix).also { if (it == -1) return missingDelimiterValue }
    return substring(prefixIndex + 1, suffixIndex)
}

/**
 * 根据指定的前后缀，得到最后一个符合条件的子字符串，如果找不到前缀或后缀，则返回默认值。
 * 默认值默认为当前字符串自身。
 */
@JvmOverloads
fun String.substringInLast(prefix: String, suffix: String, missingDelimiterValue: String = this): String {
    val prefixIndex = lastIndexOf(prefix).also { if (it == -1) return missingDelimiterValue }
    val suffixIndex = lastIndexOf(suffix).also { if (it == -1) return missingDelimiterValue }
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
    for (i in chars.indices) {
        val char = chars[i]
        if ((char == '\r' && chars[i + 1] != '\n') || char == '\n') newLine++
        if (newLine >= 2) return true
    }
    return false
}

fun Char.isExactLineBreak(): Boolean {
    return this == '\n' || this == '\r'
}

fun Char.isExactLetter(): Boolean {
    return this in 'a'..'z' || this in 'A'..'Z'
}

fun Char.isExactDigit(): Boolean {
    return this in '0'..'9'
}

fun Char.isExactWord(): Boolean {
    return this == '_' || isExactLetter() || isExactDigit()
}

fun String.isLeftQuoted(quote: Char = '"'): Boolean {
    return startsWith(quote)
}

fun String.isRightQuoted(quote: Char = '"'): Boolean {
    return length > 1 && endsWith(quote) && run {
        var n = 0
        for (i in (lastIndex - 1) downTo 0) {
            if (this[i] == '\\') n++ else break
        }
        n % 2 == 0
    }
}

fun String.isQuoted(quote: Char = '"'): Boolean {
    return isLeftQuoted(quote) || isRightQuoted(quote)
}

fun String.quote(quote: Char = '"'): String {
    val s = this
    if (s.isEmpty() || s == quote.toString()) return "$quote$quote"
    val start = isLeftQuoted(quote)
    val end = isRightQuoted(quote)
    if (start && end) return s
    return buildString {
        append(quote)
        s.forEachIndexed { i, c ->
            if (c == quote && !s.isEscapedCharAt(i)) append('\\')
            append(c)
        }
        append(quote)
    }
}

fun String.unquote(quote: Char = '"'): String {
    val s = this
    if (s.isEmpty() || s == quote.toString()) return ""
    val start = isLeftQuoted(quote)
    val end = isRightQuoted(quote)
    if (!start && !end) return s
    return buildString {
        var offset = if (start) 1 else 0
        s.forEachIndexed f@{ i, c ->
            if (start && i == 0) return@f
            if (end && i == s.lastIndex) return@f
            if (c == quote && s.isEscapedCharAt(i)) deleteCharAt(i - 1 - offset++)
            append(c)
        }
    }
}

fun String.quoteIfNecessary(quote: Char = '"'): String {
    //如果包含空白或者双引号的话要使用双引号括起
    if (any { it.isWhitespace() || it == quote }) return this.quote(quote)
    return this
}

/**
 * 判断当前字符串中的指定索引[index]的字符是否被转义。（在前面有连续的奇数个反斜线）
 */
fun String.isEscapedCharAt(index: Int): Boolean {
    if (index == 0) return false
    var n = 0
    for (i in (index - 1) downTo 0) {
        if (this[i] == '\\') n++ else break
    }
    return n % 2 == 1
}

fun String.escapeXml() = if (this.isEmpty()) "" else StringUtil.escapeXmlEntities(this)

fun String.escapeBlank(): String {
    var builder: StringBuilder? = null
    for ((i, c) in this.withIndex()) {
        if (c.isWhitespace()) {
            if (builder == null) builder = StringBuilder(substring(0, i))
            builder.append("&nbsp;")
        } else {
            builder?.append(c)
        }
    }
    return builder?.toString() ?: this
}

fun Collection<String>.toCommaDelimitedString(): String {
    val input = this
    return if (input.isEmpty()) "" else input.joinToString(",")
}

fun String.toCommaDelimitedStringList(destination: MutableList<String> = mutableListOf()): MutableList<String> {
    return this.split(',').mapNotNullTo(destination) { it.trim().orNull() }
}

fun String.toCommaDelimitedStringSet(destination: MutableSet<String> = mutableSetOf()): MutableSet<String> {
    return this.split(',').mapNotNullTo(destination) { it.trim().orNull() }
}

fun String.splitOptimized(vararg delimiters: Char, ignoreCase: Boolean = false, limit: Int = 0): List<String> {
    return this.split(*delimiters, ignoreCase = ignoreCase, limit = limit).mapNotNull { it.trim().orNull() }
}

fun String.truncate(limit: Int, ellipsis: String = "..."): String {
    return if (this.length <= limit) this else this.take(limit) + ellipsis
}

fun String.truncateAndKeepQuotes(limit: Int, ellipsis: String = "..."): String {
    if (this.isLeftQuoted()) {
        return if (this.length - 2 <= limit) this else this.take(limit + 1) + ellipsis + "\""
    } else {
        return if (this.length <= limit) this else this.take(limit) + ellipsis
    }
}

fun String.splitToPair(delimiter: Char): Pair<String, String>? {
    val index = this.indexOf(delimiter)
    if (index == -1) return null
    return this.substring(0, index) to this.substring(index + 1)
}

fun String.toCapitalizedWord(): String {
    if (isEmpty()) return this
    return this[0].uppercase() + this.substring(1)
}

fun String.toCapitalizedWords(): String {
    if (isEmpty()) return this
    return buildString {
        var isFirst = true
        var isWordStart = true
        for (c in this@toCapitalizedWords) {
            when {
                isWordStart -> {
                    isWordStart = false
                    if (isFirst) {
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

fun CharSequence.indicesOf(char: Char, startIndex: Int = 0, ignoreCase: Boolean = false, limit: Int = 0): List<Int> {
    var indices: MutableList<Int>? = null
    var lastIndex = indexOf(char, startIndex, ignoreCase)
    while (lastIndex != -1) {
        if (indices == null) indices = mutableListOf()
        indices.add(lastIndex)
        if (limit > 0 && indices.size == limit) break
        lastIndex = indexOf(char, lastIndex + 1, ignoreCase)
    }
    return indices ?: emptyList()
}

fun Collection<String>.truncate(limit: Int, ellipsis: String = "..."): List<String> {
    return take(limit).let { if (size > limit) it + ellipsis else it }
}

fun <K, V> Map<K, V>.find(predicate: (Map.Entry<K, V>) -> Boolean): V? {
    for (entry in this) {
        if (predicate(entry)) return entry.value
    }
    throw NoSuchElementException()
}

fun <K, V> Map<K, V>.findOrNull(predicate: (Map.Entry<K, V>) -> Boolean): V? {
    for (entry in this) {
        if (predicate(entry)) return entry.value
    }
    return null
}

inline fun <reified T> Any?.cast(): T = this as T

inline fun <reified T> Any?.castOrNull(): T? = this as? T

fun <C : CharSequence> C.ifNotEmpty(block: (C) -> C): C = if (this.isNotEmpty()) block(this) else this

/**
 * 判断当前输入是否匹配指定的通配符表达式。使用"?"匹配单个字符，使用"*"匹配任意个字符。
 */
fun String.matchesPattern(pattern: String, ignoreCase: Boolean = false): Boolean {
    if (pattern.isEmpty() && this.isNotEmpty()) return false
    if (pattern == "*") return true
    val cache = if (ignoreCase) patternToRegexCache2 else patternToRegexCache1
    val path0 = this
    val pattern0 = pattern
    return cache.get(pattern0).matches(path0)
}

private val patternToRegexCache1 = CacheBuilder.newBuilder().maximumSize(10000)
    .buildCache<String, Regex> { it.patternToRegexString().toRegex() }
private val patternToRegexCache2 = CacheBuilder.newBuilder().maximumSize(10000)
    .buildCache<String, Regex> { it.patternToRegexString().toRegex(RegexOption.IGNORE_CASE) }

private fun String.patternToRegexString(): String {
    val s = this
    return buildString {
        append("\\Q")
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c == '*' -> append("\\E.*\\Q")
                c == '?' -> append("\\E.\\Q")
                else -> append(c)
            }
            i++
        }
        append("\\E")
    }
}

/**
 * 判断当前输入是否匹配指定的ANT表达式。使用 "?" 匹配单个子路径中的单个字符，"*" 匹配单个子路径中的任意个字符，"**" 匹配任意个子路径。
 *
 * 这个实现的耗时约为基于正则时的一半。
 */
fun String.matchesAntPattern(pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
    return PatternMatchers.AntMatcher.matches(this, pattern, ignoreCase, trimSeparator)
}

/**
 * 判断当前输入是否匹配指定的正则表达式。
 */
fun String.matchesRegex(pattern: String, ignoreCase: Boolean = false): Boolean {
    return PatternMatchers.RegexMatcher.matches(this, pattern, ignoreCase)
}

/**
 * 判断当前路径是否匹配另一个路径（相同或者是其父路径）。使用"/"作为路径分隔符。
 * @receiver 当前路径。
 * @param other 另一个路径。
 * @param acceptSelf 是否接受路径完全一致的情况。
 * @param strict 是否严格匹配（相同或是其直接父路径）。
 * @param trim 是否需要事先去除当前路径首尾的路径分隔符.
 */
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
fun String.matchesPath(other: String, acceptSelf: Boolean = true, strict: Boolean = false, trim: Boolean = false): Boolean {
    //这个方法的执行速度应当非常非常快

    val path = if (trim) this.trimFast('/') else this
    val length = path.length
    val otherLength = other.length
    if (length > otherLength) return false
    if ((other as java.lang.String).startsWith(path, 0)) {
        if (length == otherLength) return acceptSelf
        if (other[length] != '/') return false
        if (strict && (other as java.lang.String).indexOf(47, length + 1) != -1) return false //47 -> '/'
        return true
    }
    return false
}

/**
 * 规范化当前路径。
 *
 * 将分隔符统一替换成"/"，将连续的分隔符替换为单个分隔符，并去除所有作为后缀的分隔符。
 */
fun String.normalizePath(): String {
    if (this.isEmpty()) return ""
    val builder = StringBuilder()
    var separatorFlag = false
    this.forEach { c ->
        if (c == '/' || c == '\\') {
            separatorFlag = true
        } else if (separatorFlag) {
            separatorFlag = false
            builder.append('/').append(c)
        } else {
            builder.append(c)
        }
    }
    val s = builder.toString()
    return s.trimEnd('/')
}

inline fun Path.formatted() = absolute().normalize()

fun Path.create(): Path {
    try {
        if (isDirectory()) {
            createDirectories()
        } else {
            createParentDirectories()
            createFile()
        }
    } catch (e: FileAlreadyExistsException) {
        //ignored
    }
    return this
}

fun Boolean.toByte() = if (this) 1.toByte() else 0.toByte()

fun Byte.toBoolean() = if (this == 0.toByte()) false else true

fun Boolean?.toByte() = if (this == null) 2.toByte() else toByte()

fun Byte.toBooleanOrNull() = if (this == 2.toByte()) null else toBoolean()

fun Boolean.toInt() = if (this) 1 else 0

fun Any?.toStringOrEmpty() = this?.toString() ?: ""

fun String?.toBooleanYesNo() = this.equals("yes", true)

fun String?.toBooleanYesNoOrNull() = if (this == "yes") true else if (this == "no") false else null

fun String.toUUID() = UUID.nameUUIDFromBytes(toByteArray(StandardCharsets.UTF_8))

fun String.toUuidString() = UUID.nameUUIDFromBytes(toByteArray(StandardCharsets.UTF_8)).toString()

fun String.toFile() = File(this)

fun String.toFileOrNull() = runCatchingCancelable { File(this) }.getOrNull()

fun String.toPath() = Path.of(this)

fun String.toPathOrNull() = runCatchingCancelable { Path.of(this) }.getOrNull()

fun String.toFileUrl() = File(this).toURI().toURL()

fun String.toClasspathUrl(locationClass: Class<*> = PlsFacade::class.java) = locationClass.getResource(this)!!

fun String.toClass() = Class.forName(this)

fun String.toKClass() = Class.forName(this).kotlin

fun URL.toFile() = File(this.toURI())

fun URL.toPath() = Paths.get(this.toURI())

typealias FloatRange = ClosedRange<Float>

operator fun FloatRange.contains(element: Float?) = element != null && contains(element)

@Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
fun executeCommand(
    command: String,
    commandType: CommandType? = null,
    environment: Map<String, String> = emptyMap(),
    workDirectory: File? = null,
    timeout: Long? = null
): String {
    return CommandExecutor(environment, workDirectory, timeout).execute(command, commandType)
}

@Throws(IOException::class, InterruptedException::class, CommandExecutionException::class)
fun executeCommand(
    commands: List<String>,
    environment: Map<String, String> = emptyMap(),
    workDirectory: File? = null,
    timeout: Long? = null
): String {
    return CommandExecutor(environment, workDirectory, timeout).execute(commands)
}
