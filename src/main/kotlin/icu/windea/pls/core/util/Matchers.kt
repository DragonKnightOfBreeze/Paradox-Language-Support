package icu.windea.pls.core.util

import com.google.common.cache.*
import icu.windea.pls.core.*

object Matchers {
    object GlobMatcher {
        /**
         * 判断当前输入是否匹配指定的GLOB表达式。使用 "?" 匹配单个字符，"*" 匹配任意个字符。
         */
        fun matches(input: String, pattern: String, ignoreCase: Boolean = false): Boolean {
            if (pattern.isEmpty() && input.isNotEmpty()) return false
            if (pattern == "*") return true
            return segmentMatch(input, pattern, ignoreCase)
        }

        private fun segmentMatch(input: String, pattern: String, ignoreCase: Boolean): Boolean {
            var i = 0
            var pi = 0
            var star = -1
            var match = 0
            while (i < input.length) {
                when {
                    pi < pattern.length && (pattern[pi] == '?' || pattern[pi].equals(input[i], ignoreCase)) -> {
                        i++; pi++
                    }
                    pi < pattern.length && pattern[pi] == '*' -> {
                        match = i; star = pi++
                    }
                    star != -1 -> {
                        i = ++match; pi = star + 1
                    }
                    else -> return false
                }
            }
            while (pi < pattern.length && pattern[pi] == '*') pi++
            return pi == pattern.length
        }
    }

    object AntMatcher {
        /**
         * 判断当前输入是否匹配指定的ANT表达式。使用 "?" 匹配单个子路径中的单个字符，"*" 匹配单个子路径中的任意个字符，"**" 匹配任意个子路径。
         *
         * 这个实现的耗时约为基于正则时的一半。
         */
        fun matches(input: String, pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
            if (input.isNotEmpty() && pattern.isEmpty()) return false
            val input0 = if (trimSeparator) input.trimFast('/') else input
            val pattern0 = if (trimSeparator) pattern.trimFast('/') else pattern
            if (input0.isEmpty() && (pattern0 == "**" || pattern0.all { it == '*' })) return true
            if (pattern0 == "**") return true
            val inputTokens = if (input0.isEmpty()) emptyList() else input0.splitFast('/')
            val patternTokens = if (pattern0.isEmpty()) emptyList() else pattern0.splitFast('/')
            fun match(i: Int, pi: Int): Boolean {
                var i0 = i
                var pi0 = pi
                while (i0 < inputTokens.size && pi0 < patternTokens.size) {
                    when {
                        patternTokens[pi0] == "**" -> {
                            if (match(i0, pi0 + 1)) return true
                            if (match(i0 + 1, pi0)) return true
                            return false
                        }
                        segmentMatch(inputTokens[i0], patternTokens[pi0], ignoreCase) -> {
                            i0++; pi0++
                        }
                        else -> return false
                    }
                }
                while (pi0 < patternTokens.size && patternTokens[pi0] == "**") pi0++
                return i0 == inputTokens.size && pi0 == patternTokens.size
            }
            return match(0, 0)
        }

        private fun segmentMatch(input: String, pattern: String, ignoreCase: Boolean): Boolean {
            var i = 0
            var pi = 0
            var star = -1
            var match = 0
            while (i < input.length) {
                when {
                    pi < pattern.length && (pattern[pi] == '?' || pattern[pi].equals(input[i], ignoreCase)) -> {
                        i++; pi++
                    }
                    pi < pattern.length && pattern[pi] == '*' -> {
                        match = i; star = pi++
                    }
                    star != -1 -> {
                        i = ++match; pi = star + 1
                    }
                    else -> return false
                }
            }
            while (pi < pattern.length && pattern[pi] == '*') pi++
            return pi == pattern.length
        }
    }

    @Deprecated(message = "", replaceWith = ReplaceWith("AntMatcher"))
    object AntFromRegexMatcher {
        /**
         * 判断当前输入是否匹配指定的ANT表达式。使用 "?" 匹配单个子路径中的单个字符，"*" 匹配单个子路径中的任意个字符，"**" 匹配任意个子路径。
         */
        fun matches(input: String, pattern: String, ignoreCase: Boolean = false): Boolean {
            if (pattern.isEmpty() && input.isNotEmpty()) return false
            val cache = if (ignoreCase) regexCache2 else regexCache1
            val path0 = input
            val pattern0 = pattern.antPatternToRegexString()
            return cache.get(pattern0).matches(path0)
        }

        private val regexCache1 by lazy {
            CacheBuilder.newBuilder().maximumSize(10000).buildCache<String, Regex> { it.toRegex() }
        }
        private val regexCache2 by lazy {
            CacheBuilder.newBuilder().maximumSize(10000).buildCache<String, Regex> { it.toRegex(RegexOption.IGNORE_CASE) }
        }

        private fun String.antPatternToRegexString(): String {
            val s = this
            var r = buildString {
                append("\\Q")
                var i = 0
                while (i < s.length) {
                    val c = s[i]
                    when {
                        c == '*' -> {
                            val nc = s.getOrNull(i + 1)
                            if (nc == '*') {
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
            }
            r = r.replace("\\E\\Q", "")
            r = r.replace("/\\E.*\\Q/", "\\E(/[^/]*)*\\Q")
            return r
        }
    }

    object RegexMatcher {
        /**
         * 判断当前输入是否匹配指定的正则表达式。
         */
        fun matches(input: String, pattern: String, ignoreCase: Boolean = false): Boolean {
            if (pattern.isEmpty() && input.isNotEmpty()) return false
            val cache = if (ignoreCase) regexCache2 else regexCache1
            val path0 = input
            val pattern0 = pattern
            return cache.get(pattern0).matches(path0)
        }

        private val regexCache1 by lazy {
            CacheBuilder.newBuilder().maximumSize(10000).buildCache<String, Regex> { it.toRegex() }
        }
        private val regexCache2 by lazy {
            CacheBuilder.newBuilder().maximumSize(10000).buildCache<String, Regex> { it.toRegex(RegexOption.IGNORE_CASE) }
        }
    }

    object PathMatcher {
        /**
         * 判断输入的子路径是否匹配指定的子路径。
         *
         * @param input 输入的子路径。
         * @param other 指定的子路径。
         * @param ignoreCase 是否忽略大小写。
         * @param useAny 如果启用，且用来匹配的子路径是`"any"`，则表示匹配任意子路径。
         * @param usePattern 如果启用，用来匹配的子路径可以是一个GLOB表达式。参见：[GlobMatcher]。
         */
        fun matches(input: String, other: String, ignoreCase: Boolean = false, useAny: Boolean = false, usePattern: Boolean = false): Boolean {
            if (useAny && other == "any") return true
            if (usePattern) return GlobMatcher.matches(input, other, ignoreCase)
            return input.equals(other, ignoreCase)
        }

        /**
         * 判断输入的子路径列表是否匹配指定的子路径列表。
         *
         * @param input 输入的子路径列表。
         * @param other 指定的子路径列表。
         * @param ignoreCase 是否忽略大小写。
         * @param useAny 如果启用，且用来匹配的子路径是`"any"`，则表示匹配任意子路径。
         * @param usePattern 如果启用，用来匹配的子路径可以是一个GLOB表达式。参见：[GlobMatcher]。
         */
        fun matches(input: List<String>, other: List<String>, ignoreCase: Boolean = false, useAny: Boolean = false, usePattern: Boolean = false): Boolean {
            if (input.size != other.size) return false //路径过短或路径长度不一致
            for ((index, otherPath) in other.withIndex()) {
                val inputPath = input[index]
                val r = matches(inputPath, otherPath, ignoreCase, useAny, usePattern)
                if (!r) return false
            }
            return true
        }

        /**
         * 得到输入的子路径列表相对于指定的子路径列表的第一个子路径。如果两者完全匹配，则返回空字符串。
         *
         * 例如，`"/foo/bar/x" relativeTo "/foo" -> "bar"`。
         *
         * @param input 输入的子路径列表。
         * @param other 指定的子路径列表。
         * @param ignoreCase 是否忽略大小写。
         * @param useAny 如果启用，且用来匹配的子路径是`"any"`，则表示匹配任意子路径。
         * @param usePattern 如果启用，用来匹配的子路径可以是一个GLOB表达式。参见：[GlobMatcher]。
         */
        fun relative(input: List<String>, other: List<String>, ignoreCase: Boolean = false, useAny: Boolean = false, usePattern: Boolean = false): String? {
            if (input.size > other.size) return null
            for ((index, inputPath) in input.withIndex()) {
                val otherPath = other[index]
                val r = matches(inputPath, otherPath, ignoreCase, useAny, usePattern)
                if (!r) return null
            }
            if (input.size == other.size) return ""
            return other[input.size]
        }
    }
}
