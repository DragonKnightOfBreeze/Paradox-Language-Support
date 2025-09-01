@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core

import com.intellij.openapi.util.text.StringUtil
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.util.Matchers
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.isDirectory
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

private data object EmptyObject

/** 空对象常量。可用于占位。 */
val EMPTY_OBJECT: Any = EmptyObject

/** 空操作，占位用。 */
inline fun pass() {}

/** 当条件为真时调用 [block]，否则返回接收者本身。 */
@OptIn(ExperimentalContracts::class)
inline fun <T : R, R> T.letIf(condition: Boolean, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (condition) block(this) else this
}

/** 当条件为假时调用 [block]，否则返回接收者本身。 */
@OptIn(ExperimentalContracts::class)
inline fun <T : R, R> T.letUnless(condition: Boolean, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (!condition) block(this) else this
}

/** 当条件为真时对接收者执行副作用 [block]，并返回接收者。 */
@OptIn(ExperimentalContracts::class)
inline fun <T> T.alsoIf(condition: Boolean, block: (T) -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    if (condition) block(this)
    return this
}

/** 当条件为假时对接收者执行副作用 [block]，并返回接收者。 */
@OptIn(ExperimentalContracts::class)
inline fun <T> T.alsoUnless(condition: Boolean, block: (T) -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    if (!condition) block(this)
    return this
}

/** 判断是否为非空且非空字符串。 */
@OptIn(ExperimentalContracts::class)
@Suppress("ReplaceSizeCheckWithIsNotEmpty")
inline fun CharSequence?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }

    return this != null && this.length != 0
}

/** 判断是否为非空且元素数量大于 0 的数组。 */
@OptIn(ExperimentalContracts::class)
@Suppress("ReplaceSizeCheckWithIsNotEmpty")
inline fun Array<*>?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }

    return this != null && this.size != 0
}

/** 判断是否为非空且元素数量大于 0 的集合。 */
@OptIn(ExperimentalContracts::class)
@Suppress("ReplaceSizeCheckWithIsNotEmpty")
inline fun Collection<*>?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }

    return this != null && this.size != 0
}

/** 按位数进行格式化：正数四舍五入到 10^digits，负数保留小数位。 */
fun Number.format(digits: Int): String {
    val power = 10.0.pow(abs(digits))
    return when {
        digits > 0 -> (round(this.toLong() / power) * power).toLong().toString()
        digits == 0 -> this.toLong().toString()
        else -> (round(this.toDouble() * power) / power).toString()
            .let { if (it.lastIndexOf('.') == -1) "$it.0" else it }
    }
}

/** 非空字符串返回自身，否则返回 null。 */
inline fun <T : CharSequence> T.orNull() = this.takeIf { it.isNotEmpty() }

/** 判断是否以指定前缀与后缀包裹。 */
fun CharSequence.surroundsWith(prefix: Char, suffix: Char, ignoreCase: Boolean = false): Boolean {
    return startsWith(prefix, ignoreCase) && endsWith(suffix, ignoreCase)
}

/** 判断是否以指定前缀与后缀包裹。 */
fun CharSequence.surroundsWith(prefix: CharSequence, suffix: CharSequence, ignoreCase: Boolean = false): Boolean {
    return endsWith(suffix, ignoreCase) && startsWith(prefix, ignoreCase) //先匹配后缀，这样可能会提高性能
}

/** 在开头添加前缀并返回新字符串。 */
fun CharSequence.addPrefix(prefix: CharSequence): String {
    return prefix.toString() + this.toString()
}

/** 在结尾添加后缀并返回新字符串。 */
fun CharSequence.addSuffix(suffix: CharSequence): String {
    return this.toString() + suffix.toString()
}

/** 在两侧添加前后缀并返回新字符串。 */
fun CharSequence.addSurrounding(prefix: CharSequence, suffix: CharSequence): String {
    return prefix.toString() + this.toString() + suffix.toString()
}

/** 移除两侧指定的前后缀。若任一不匹配则原样返回。 */
fun CharSequence.removeSurrounding(prefix: CharSequence, suffix: CharSequence): CharSequence {
    return removePrefix(prefix).removeSuffix(suffix)
}

/** 移除两侧指定的前后缀。若任一不匹配则原样返回。 */
fun String.removeSurrounding(prefix: CharSequence, suffix: CharSequence): String {
    return removePrefix(prefix).removeSuffix(suffix)
}

/** 若存在指定前缀则移除并返回，否则返回 null。 */
fun CharSequence.removePrefixOrNull(prefix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (startsWith(prefix, ignoreCase)) substring(prefix.length) else null
}

/** 若存在指定前缀则移除并返回，否则返回 null。 */
fun String.removePrefixOrNull(prefix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (startsWith(prefix, ignoreCase)) substring(prefix.length) else null
}

/** 若存在指定后缀则移除并返回，否则返回 null。 */
fun CharSequence.removeSuffixOrNull(suffix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (endsWith(suffix, ignoreCase)) substring(0, length - suffix.length) else null
}

/** 若存在指定后缀则移除并返回，否则返回 null。 */
fun String.removeSuffixOrNull(suffix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (endsWith(suffix, ignoreCase)) substring(0, length - suffix.length) else null
}

/** 若两侧存在指定前后缀则移除并返回，否则返回 null。 */
fun CharSequence.removeSurroundingOrNull(prefix: CharSequence, suffix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (surroundsWith(prefix, suffix, ignoreCase)) substring(prefix.length, length - suffix.length) else null
}

/** 若两侧存在指定前后缀则移除并返回，否则返回 null。 */
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

/** 按空白分割字符串（连续空白视为一个分隔符）。 */
fun String.splitByBlank(limit: Int = 0): List<String> {
    return split(blankRegex, limit)
}

/** 是否包含任意空白字符。 */
fun String.containsBlank(): Boolean {
    return any { it.isWhitespace() }
}

/** 是否包含换行符（\n 或 \r）。 */
fun String.containsLineBreak(): Boolean {
    return any { it == '\n' || it == '\r' }
}

/** 是否包含空行。 */
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

/** 是否为换行符（严格判断）。 */
fun Char.isExactLineBreak(): Boolean {
    return this == '\n' || this == '\r'
}

/** 是否为英文字母（严格判断）。 */
fun Char.isExactLetter(): Boolean {
    return this in 'a'..'z' || this in 'A'..'Z'
}

/** 是否为数字字符（严格判断）。 */
fun Char.isExactDigit(): Boolean {
    return this in '0'..'9'
}

/** 是否为“单词”字符：字母/数字/下划线。 */
fun Char.isExactWord(): Boolean {
    return this == '_' || isExactLetter() || isExactDigit()
}

/** 是否以指定引号开头。 */
fun String.isLeftQuoted(quote: Char = '"'): Boolean {
    return startsWith(quote)
}

/** 是否以指定引号结尾（考虑反斜杠转义）。 */
fun String.isRightQuoted(quote: Char = '"'): Boolean {
    return length > 1 && endsWith(quote) && run {
        var n = 0
        for (i in (lastIndex - 1) downTo 0) {
            if (this[i] == '\\') n++ else break
        }
        n % 2 == 0
    }
}

/** 是否被指定引号包裹（仅需匹配一侧）。 */
fun String.isQuoted(quote: Char = '"'): Boolean {
    return isLeftQuoted(quote) || isRightQuoted(quote)
}

/** 为字符串加上引号并转义内部同类引号。 */
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

/** 去除字符串两侧引号并反转义内部同类引号。 */
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

/** 若包含空白/引号或 [extraChars] 中的字符，则为字符串加引号。 */
fun String.quoteIfNecessary(quote: Char = '"', extraChars: String = "", blank: Boolean = true): String {
    val shouldQuote = this.any { it == quote || (blank && it.isWhitespace()) || it in extraChars }
    return if (shouldQuote) this.quote(quote) else this
}

/** 判断索引 [index] 处字符是否被转义（前有奇数个反斜杠）。 */
fun String.isEscapedCharAt(index: Int): Boolean {
    if (index == 0) return false
    var n = 0
    for (i in (index - 1) downTo 0) {
        if (this[i] == '\\') n++ else break
    }
    return n % 2 == 1
}

/** 转义为 XML 实体。 */
fun String.escapeXml() = if (this.isEmpty()) "" else StringUtil.escapeXmlEntities(this)

/** 将空白字符替换为“&nbsp;”。 */
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

/** 以逗号连接字符串集合。 */
fun Collection<String>.toCommaDelimitedString(): String {
    val input = this
    return if (input.isEmpty()) "" else input.joinToString(",")
}

/** 将逗号分隔的字符串解析为去空白的字符串列表。 */
fun String.toCommaDelimitedStringList(destination: MutableList<String> = mutableListOf()): MutableList<String> {
    return this.split(',').mapNotNullTo(destination) { it.trim().orNull() }
}

/** 将逗号分隔的字符串解析为去空白的字符串集合。 */
fun String.toCommaDelimitedStringSet(destination: MutableSet<String> = mutableSetOf()): MutableSet<String> {
    return this.split(',').mapNotNullTo(destination) { it.trim().orNull() }
}

/** 按多个分隔符拆分并去除空白项。 */
fun String.splitOptimized(vararg delimiters: Char, ignoreCase: Boolean = false, limit: Int = 0): List<String> {
    return this.split(*delimiters, ignoreCase = ignoreCase, limit = limit).mapNotNull { it.trim().orNull() }
}

/** 将字符串截断到 [limit] 长度并追加省略号。 */
fun String.truncate(limit: Int, ellipsis: String = "..."): String {
    if (limit <= 0) return this
    return if (this.length <= limit) this else this.take(limit) + ellipsis
}

/** 截断字符串时尽量保持成对引号。 */
fun String.truncateAndKeepQuotes(limit: Int, ellipsis: String = "..."): String {
    if (limit <= 0) return this
    if (this.isLeftQuoted()) {
        return if (this.length - 2 <= limit) this else this.take(limit + 1) + ellipsis + "\""
    } else {
        return if (this.length <= limit) this else this.take(limit) + ellipsis
    }
}

/** 以首个分隔符拆分为二元组，找不到则返回 null。 */
fun String.splitToPair(delimiter: Char): Pair<String, String>? {
    val index = this.indexOf(delimiter)
    if (index == -1) return null
    return this.substring(0, index) to this.substring(index + 1)
}

/** 将首字符转为大写。 */
fun String.capitalized(): String {
    return replaceFirstChar { it.uppercaseChar() }
}

/** 将首字符转为小写。 */
fun String.decapitalized(): String {
    return replaceFirstChar { it.lowercaseChar() }
}

/** 将“_ - .”视为分隔符并转为“Title Case”。 */
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

/** 返回字符在序列中的所有起始索引。 */
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

/** 返回字符串在序列中的所有起始索引。 */
fun CharSequence.indicesOf(text: String, startIndex: Int = 0, ignoreCase: Boolean = false, limit: Int = 0): List<Int> {
    var indices: MutableList<Int>? = null
    var lastIndex = indexOf(text, startIndex, ignoreCase)
    while (lastIndex != -1) {
        if (indices == null) indices = mutableListOf()
        indices.add(lastIndex)
        if (limit > 0 && indices.size == limit) break
        lastIndex = indexOf(text, lastIndex + 1, ignoreCase)
    }
    return indices ?: emptyList()
}

/** 截断集合到 [limit] 个元素；超出时在末尾追加省略号。 */
fun Collection<String>.truncate(limit: Int, ellipsis: String = "..."): List<String> {
    return take(limit).let { if (size > limit) it + ellipsis else it }
}

/** 非空强转为类型 [T]。 */
inline fun <reified T> Any?.cast(): T = this as T

/** 尝试转为类型 [T]，失败返回 null。 */
inline fun <reified T> Any?.castOrNull(): T? = this as? T

/** 若非空则应用 [block]，否则原样返回。 */
fun <C : CharSequence> C.ifNotEmpty(block: (C) -> C): C = if (this.isNotEmpty()) block(this) else this

/**
 * 判断当前输入是否匹配指定的GLOB表达式。使用 "?" 匹配单个字符，"*" 匹配任意个字符。
 */
inline fun String.matchesPattern(pattern: String, ignoreCase: Boolean = false): Boolean {
    return Matchers.GlobMatcher.matches(this, pattern, ignoreCase)
}

/**
 * 判断当前输入是否匹配指定的ANT表达式。使用 "?" 匹配单个子路径中的单个字符，"*" 匹配单个子路径中的任意个字符，"**" 匹配任意个子路径。
 *
 * 这个实现的耗时约为基于正则时的一半。
 */
inline fun String.matchesAntPattern(pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
    return Matchers.AntMatcher.matches(this, pattern, ignoreCase, trimSeparator)
}

/**
 * 判断当前输入是否匹配指定的正则表达式。
 */
inline fun String.matchesRegex(pattern: String, ignoreCase: Boolean = false): Boolean {
    return Matchers.RegexMatcher.matches(this, pattern, ignoreCase)
}

/**
 * 判断当前路径是否匹配另一个路径（相同或者是其父路径）。
 *
 * 使用"/"作为路径分隔符。
 * 不会忽略前导的路径分隔符。
 *
 * @param other 另一个路径。
 * @param acceptSelf 是否接受路径完全一致的情况。
 * @param strict 是否严格匹配（相同或是其直接父路径）。
 * @param trim 是否需要事先去除当前路径首尾的路径分隔符。不会去除另一个路径首尾的路径分隔符。
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

/** 归一化为绝对规范路径。 */
inline fun Path.formatted() = absolute().normalize()

/** 若为目录则创建目录树；否则创建文件并保证父目录存在。 */
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

/** true->1，false->0。 */
fun Boolean.toByte() = if (this) 1.toByte() else 0.toByte()

/** 0->false，非 0->true。 */
fun Byte.toBoolean() = if (this == 0.toByte()) false else true

/** null->2，true->1，false->0。 */
fun Boolean?.toByte() = if (this == null) 2.toByte() else toByte()

/** 2->null，其余同 [toBoolean]。 */
fun Byte.toBooleanOrNull() = if (this == 2.toByte()) null else toBoolean()

/** true->1，false->0。 */
fun Boolean.toInt() = if (this) 1 else 0

/** 将 null 安全地转为空串。 */
fun Any?.toStringOrEmpty() = this?.toString() ?: ""

/** 将字符串 yes/no 转为布尔（忽略大小写，仅 yes 为真）。 */
fun String?.toBooleanYesNo() = this.equals("yes", true)

/** 将字符串 yes/no 转为布尔，其他返回 null。 */
fun String?.toBooleanYesNoOrNull() = if (this == "yes") true else if (this == "no") false else null

/** 基于名称（UTF-8 字节）生成稳定 UUID。 */
fun String.toUUID() = UUID.nameUUIDFromBytes(toByteArray(StandardCharsets.UTF_8))

/** 基于名称生成稳定 UUID 字符串。 */
fun String.toUuidString() = UUID.nameUUIDFromBytes(toByteArray(StandardCharsets.UTF_8)).toString()

/** 转为 File。 */
fun String.toFile() = File(this)

/** 安全地转为 File，失败返回 null。 */
fun String.toFileOrNull() = runCatchingCancelable { File(this) }.getOrNull()

/** 转为 Path。 */
fun String.toPath() = Path.of(this)

/** 安全地转为 Path，失败返回 null。 */
fun String.toPathOrNull() = runCatchingCancelable { Path.of(this) }.getOrNull()

/** 转为 file:// URL。 */
fun String.toFileUrl() = File(this).toURI().toURL()

/** 从类路径查找资源并返回 URL。找不到将抛出异常。 */
fun String.toClasspathUrl(locationClass: Class<*> = PlsFacade::class.java) = locationClass.getResource(this)!!

/** 通过类名加载 Java Class。 */
fun String.toClass() = Class.forName(this)

/** 通过类名加载 Kotlin KClass。 */
fun String.toKClass() = Class.forName(this).kotlin

/** URL 转为 File。 */
fun URL.toFile() = File(this.toURI())

/** URL 转为 Path。 */
fun URL.toPath() = Paths.get(this.toURI())

/** 浮点闭区间类型别名。 */
typealias FloatRange = ClosedRange<Float>

/** 支持 `in` 检查可空浮点数是否落在区间内。 */
operator fun FloatRange.contains(element: Float?) = element != null && contains(element)
