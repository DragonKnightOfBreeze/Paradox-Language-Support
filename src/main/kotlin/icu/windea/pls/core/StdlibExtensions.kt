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

/** 通用空对象占位符。*/
val EMPTY_OBJECT: Any = EmptyObject

/** 空操作，占位用。*/
@Suppress("EmptyMethod")
inline fun pass() {}

@OptIn(ExperimentalContracts::class)
/** 若 [condition] 为 `true`，对接收者执行 [block] 并返回结果，否则返回接收者本身。*/
inline fun <T : R, R> T.letIf(condition: Boolean, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (condition) block(this) else this
}

@OptIn(ExperimentalContracts::class)
/** 若 [condition] 为 `false`，对接收者执行 [block] 并返回结果，否则返回接收者本身。*/
inline fun <T : R, R> T.letUnless(condition: Boolean, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (!condition) block(this) else this
}

@OptIn(ExperimentalContracts::class)
/** 若 [condition] 为 `true`，对接收者执行 [block] 后返回接收者本身。*/
inline fun <T> T.alsoIf(condition: Boolean, block: (T) -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    if (condition) block(this)
    return this
}

@OptIn(ExperimentalContracts::class)
/** 若 [condition] 为 `false`，对接收者执行 [block] 后返回接收者本身。*/
inline fun <T> T.alsoUnless(condition: Boolean, block: (T) -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    if (!condition) block(this)
    return this
}

@OptIn(ExperimentalContracts::class)
@Suppress("ReplaceSizeCheckWithIsNotEmpty")
/** 判断可空字符序列是否非空（同时判空与长度为 0）。*/
inline fun CharSequence?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }

    return this != null && this.length != 0
}

@OptIn(ExperimentalContracts::class)
@Suppress("ReplaceSizeCheckWithIsNotEmpty")
/** 判断可空数组是否非空。*/
inline fun Array<*>?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }

    return this != null && this.size != 0
}

@OptIn(ExperimentalContracts::class)
@Suppress("ReplaceSizeCheckWithIsNotEmpty")
/** 判断可空集合是否非空。*/
inline fun Collection<*>?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }

    return this != null && this.size != 0
}

/**
 * 将数字按 [digits] 位进行格式化。
 *
 * - [digits] > 0：向最接近的 10^digits 值取整（保留整数）；
 * - [digits] = 0：转为整数；
 * - [digits] < 0：按小数位四舍五入。
 */
fun Number.format(digits: Int): String {
    val power = 10.0.pow(abs(digits))
    return when {
        digits > 0 -> (round(this.toLong() / power) * power).toLong().toString()
        digits == 0 -> this.toLong().toString()
        else -> (round(this.toDouble() * power) / power).toString()
            .let { if (it.lastIndexOf('.') == -1) "$it.0" else it }
    }
}

/** 如果当前字符串为空，则返回 `null`。否则返回自身。*/
inline fun <T : CharSequence> T.orNull() = this.takeIf { it.isNotEmpty() }

/** 判断是否以指定前缀/后缀包裹（基于单个字符）。*/
fun CharSequence.surroundsWith(prefix: Char, suffix: Char, ignoreCase: Boolean = false): Boolean {
    return startsWith(prefix, ignoreCase) && endsWith(suffix, ignoreCase)
}

/** 判断是否以指定前缀/后缀包裹（先匹配后缀以略微优化）。*/
fun CharSequence.surroundsWith(prefix: CharSequence, suffix: CharSequence, ignoreCase: Boolean = false): Boolean {
    return endsWith(suffix, ignoreCase) && startsWith(prefix, ignoreCase) // 先匹配后缀，这样可能会提高性能
}

/** 返回添加前缀后的字符串。*/
fun CharSequence.addPrefix(prefix: CharSequence): String {
    return prefix.toString() + this.toString()
}

/** 返回添加后缀后的字符串。*/
fun CharSequence.addSuffix(suffix: CharSequence): String {
    return this.toString() + suffix.toString()
}

/** 返回添加前后缀后的字符串。*/
fun CharSequence.addSurrounding(prefix: CharSequence, suffix: CharSequence): String {
    return prefix.toString() + this.toString() + suffix.toString()
}

/** 去除成对的前后缀（若存在）。*/
fun CharSequence.removeSurrounding(prefix: CharSequence, suffix: CharSequence): CharSequence {
    return removePrefix(prefix).removeSuffix(suffix)
}

/** 去除成对的前后缀（若存在）。*/
fun String.removeSurrounding(prefix: CharSequence, suffix: CharSequence): String {
    return removePrefix(prefix).removeSuffix(suffix)
}

/** 若存在指定前缀则去除并返回，否则返回 `null`。*/
fun CharSequence.removePrefixOrNull(prefix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (startsWith(prefix, ignoreCase)) substring(prefix.length) else null
}

/** 若存在指定前缀则去除并返回，否则返回 `null`。*/
fun String.removePrefixOrNull(prefix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (startsWith(prefix, ignoreCase)) substring(prefix.length) else null
}

/** 若存在指定后缀则去除并返回，否则返回 `null`。*/
fun CharSequence.removeSuffixOrNull(suffix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (endsWith(suffix, ignoreCase)) substring(0, length - suffix.length) else null
}

/** 若存在指定后缀则去除并返回，否则返回 `null`。*/
fun String.removeSuffixOrNull(suffix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (endsWith(suffix, ignoreCase)) substring(0, length - suffix.length) else null
}

/** 若存在成对前后缀则去除并返回，否则返回 `null`。*/
fun CharSequence.removeSurroundingOrNull(prefix: CharSequence, suffix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (surroundsWith(prefix, suffix, ignoreCase)) substring(prefix.length, length - suffix.length) else null
}

/** 若存在成对前后缀则去除并返回，否则返回 `null`。*/
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

/** 按空白（\\s+）拆分为字符串列表。*/
fun String.splitByBlank(limit: Int = 0): List<String> {
    return split(blankRegex, limit)
}

/** 是否包含任意空白字符。*/
fun String.containsBlank(): Boolean {
    return any { it.isWhitespace() }
}

/** 是否包含换行符。*/
fun String.containsLineBreak(): Boolean {
    return any { it == '\n' || it == '\r' }
}

/** 是否包含空白行（连续两个换行）。*/
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

/** 是否为精确换行符（'\n' 或 '\r'）。*/
fun Char.isExactLineBreak(): Boolean {
    return this == '\n' || this == '\r'
}

/** 是否为英文字母（A-Z 或 a-z）。*/
fun Char.isExactLetter(): Boolean {
    return this in 'a'..'z' || this in 'A'..'Z'
}

/** 是否为数字（0-9）。*/
fun Char.isExactDigit(): Boolean {
    return this in '0'..'9'
}

/** 是否为单词字符（字母/数字/下划线）。*/
fun Char.isExactWord(): Boolean {
    return this == '_' || isExactLetter() || isExactDigit()
}

/** 是否以 [quote] 起始。*/
fun String.isLeftQuoted(quote: Char = '"'): Boolean {
    return startsWith(quote)
}

/** 是否以 [quote] 结尾（考虑转义）。*/
fun String.isRightQuoted(quote: Char = '"'): Boolean {
    return length > 1 && endsWith(quote) && run {
        var n = 0
        for (i in (lastIndex - 1) downTo 0) {
            if (this[i] == '\\') n++ else break
        }
        n % 2 == 0
    }
}

/** 是否左右被 [quote] 包裹。*/
fun String.isQuoted(quote: Char = '"'): Boolean {
    return isLeftQuoted(quote) || isRightQuoted(quote)
}

/** 为字符串添加引号，并对内部的引号进行转义。*/
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

/** 去除包裹引号，并对内部已转义的引号进行反转义。*/
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

/** 若包含空白/引号/额外字符，则自动加引号。*/
fun String.quoteIfNecessary(quote: Char = '"', extraChars: String = "", blank: Boolean = true): String {
    val shouldQuote = this.any { it == quote || (blank && it.isWhitespace()) || it in extraChars }
    return if (shouldQuote) this.quote(quote) else this
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

/** 转义 XML 特殊字符。*/
fun String.escapeXml() = if (this.isEmpty()) "" else StringUtil.escapeXmlEntities(this)

/** 将空白渲染为 `&nbsp;`，便于在 HTML 中显示。*/
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

/** 将字符串集合拼接为以逗号分隔的字符串。*/
fun Collection<String>.toCommaDelimitedString(): String {
    val input = this
    return if (input.isEmpty()) "" else input.joinToString(",")
}

/** 将逗号分隔字符串解析为列表（自动 trim，忽略空项）。*/
fun String.toCommaDelimitedStringList(destination: MutableList<String> = mutableListOf()): MutableList<String> {
    return this.split(',').mapNotNullTo(destination) { it.trim().orNull() }
}

/** 将逗号分隔字符串解析为集合（自动 trim，忽略空项）。*/
fun String.toCommaDelimitedStringSet(destination: MutableSet<String> = mutableSetOf()): MutableSet<String> {
    return this.split(',').mapNotNullTo(destination) { it.trim().orNull() }
}

/** 拆分后逐项 trim 并丢弃空串的轻量实现。*/
fun String.splitOptimized(vararg delimiters: Char, ignoreCase: Boolean = false, limit: Int = 0): List<String> {
    return this.split(*delimiters, ignoreCase = ignoreCase, limit = limit).mapNotNull { it.trim().orNull() }
}

/** 超出 [limit] 时截断并追加 [ellipsis]。*/
fun String.truncate(limit: Int, ellipsis: String = "..."): String {
    if (limit <= 0) return this
    return if (this.length <= limit) this else this.take(limit) + ellipsis
}

/** 截断字符串但尽量保留引号对称性。*/
fun String.truncateAndKeepQuotes(limit: Int, ellipsis: String = "..."): String {
    if (limit <= 0) return this
    if (this.isLeftQuoted()) {
        return if (this.length - 2 <= limit) this else this.take(limit + 1) + ellipsis + "\""
    } else {
        return if (this.length <= limit) this else this.take(limit) + ellipsis
    }
}

/** 以单个字符分隔符拆分为键值对。找不到分隔符时返回 `null`。*/
fun String.splitToPair(delimiter: Char): Pair<String, String>? {
    val index = this.indexOf(delimiter)
    if (index == -1) return null
    return this.substring(0, index) to this.substring(index + 1)
}

/** 首字母大写。*/
fun String.capitalized(): String {
    return replaceFirstChar { it.uppercaseChar() }
}

/** 首字母小写。*/
fun String.decapitalized(): String {
    return replaceFirstChar { it.lowercaseChar() }
}

/** 将字符串按单词边界转换为“每个单词首字母大写”形式。*/
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

/** 查找字符 [char] 在当前序列中的所有出现位置下标。*/
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

/** 查找 [text] 在当前序列中的所有出现位置下标。*/
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

/** 截断到前 [limit] 项，超出则在末尾追加 [ellipsis]。*/
fun Collection<String>.truncate(limit: Int, ellipsis: String = "..."): List<String> {
    return take(limit).let { if (size > limit) it + ellipsis else it }
}

/** 非空断言式转换（不安全）：将对象强转为 [T]。*/
inline fun <reified T> Any?.cast(): T = this as T

/** 安全转换：将对象尝试转换为 [T]，失败返回 `null`。*/
inline fun <reified T> Any?.castOrNull(): T? = this as? T

/** 若接收者非空则应用 [block]，否则返回接收者。*/
fun <C : CharSequence> C.ifNotEmpty(block: (C) -> C): C = if (this.isNotEmpty()) block(this) else this

/**
 * 判断当前输入是否匹配指定的GLOB表达式。使用 "?" 匹配单个字符，"*" 匹配任意个字符。
 */
fun String.matchesPattern(pattern: String, ignoreCase: Boolean = false): Boolean {
    return Matchers.GlobMatcher.matches(this, pattern, ignoreCase)
}

/**
 * 判断当前输入是否匹配指定的ANT表达式。使用 "?" 匹配单个子路径中的单个字符，"*" 匹配单个子路径中的任意个字符，"**" 匹配任意个子路径。
 *
 * 这个实现的耗时约为基于正则时的一半。
 */
fun String.matchesAntPattern(pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
    return Matchers.AntMatcher.matches(this, pattern, ignoreCase, trimSeparator)
}

/**
 * 判断当前输入是否匹配指定的正则表达式。
 */
fun String.matchesRegex(pattern: String, ignoreCase: Boolean = false): Boolean {
    return Matchers.RegexMatcher.matches(this, pattern, ignoreCase)
}

/**
 * 判断当前路径是否匹配另一个路径（相同或者是其父路径）。
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
    // 这个方法的执行速度应当非常非常快

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

/** 返回规范化后的绝对路径（absolute + normalize）。*/
inline fun Path.formatted() = absolute().normalize()

/** 若路径为目录则确保存在；若为文件则确保父目录存在并创建空文件（忽略已存在）。*/
fun Path.create(): Path {
    try {
        if (isDirectory()) {
            createDirectories()
        } else {
            createParentDirectories()
            createFile()
        }
    } catch (e: FileAlreadyExistsException) {
        // ignored
    }
    return this
}

/** `Boolean` 与 `Byte` 的互转：`true->1`，`false->0`。*/
fun Boolean.toByte() = if (this) 1.toByte() else 0.toByte()

/** `Byte` 转 `Boolean`：`0->false`，其他->`true`。*/
fun Byte.toBoolean() = if (this == 0.toByte()) false else true

/** 可空 `Boolean` 转 `Byte`：`null->2`。*/
fun Boolean?.toByte() = if (this == null) 2.toByte() else toByte()

/** `Byte` 转可空 `Boolean`：`2->null`，其余同上。*/
fun Byte.toBooleanOrNull() = if (this == 2.toByte()) null else toBoolean()

/** `Boolean` 转 `Int`：`true->1`，`false->0`。*/
fun Boolean.toInt() = if (this) 1 else 0

/** `null` 则返回空字符串。*/
fun Any?.toStringOrEmpty() = this?.toString() ?: ""

/** "yes"/"no" 到布尔的转换（忽略大小写）。*/
fun String?.toBooleanYesNo() = this.equals("yes", true)

/** "yes"/"no" 到可空布尔的转换："yes"->true，"no"->false，其他->null。*/
fun String?.toBooleanYesNoOrNull() = if (this == "yes") true else if (this == "no") false else null

/** 生成基于内容的稳定 UUID。*/
fun String.toUUID() = UUID.nameUUIDFromBytes(toByteArray(StandardCharsets.UTF_8))

/** 生成基于内容的稳定 UUID 字符串。*/
fun String.toUuidString() = UUID.nameUUIDFromBytes(toByteArray(StandardCharsets.UTF_8)).toString()

/** 路径与 URL/类加载器相关的便捷转换。*/
fun String.toFile() = File(this)

/** 安全转换为 [File]，失败返回 `null`。*/
fun String.toFileOrNull() = runCatchingCancelable { File(this) }.getOrNull()

/** 转换为 [Path]。*/
fun String.toPath() = Path.of(this)

/** 安全转换为 [Path]，失败返回 `null`。*/
fun String.toPathOrNull() = runCatchingCancelable { Path.of(this) }.getOrNull()

/** 转换为文件 URL（file://）。*/
fun String.toFileUrl() = File(this).toURI().toURL()

/** 基于类加载器从 classpath 获取资源 URL。*/
fun String.toClasspathUrl(locationClass: Class<*> = PlsFacade::class.java) = locationClass.getResource(this)!!

/** 反射获取 [Class]。*/
fun String.toClass() = Class.forName(this)

/** 反射获取 [kotlin.reflect.KClass]。*/
fun String.toKClass() = Class.forName(this).kotlin

/** 将 URL 转换为 [File]。*/
fun URL.toFile() = File(this.toURI())

/** 将 URL 转换为 [Path]。*/
fun URL.toPath() = Paths.get(this.toURI())

typealias FloatRange = ClosedRange<Float>

/** 允许对可空 Float 使用 `in` 检查（仅在非空时为真参与判断）。*/
operator fun FloatRange.contains(element: Float?) = element != null && contains(element)
