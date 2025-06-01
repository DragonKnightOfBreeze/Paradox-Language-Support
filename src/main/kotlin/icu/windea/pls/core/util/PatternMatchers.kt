package icu.windea.pls.core.util

import com.google.common.cache.*
import icu.windea.pls.core.*

interface PatternMatchers {
    object AntMatcher {
        /**
         * 判断当前输入是否匹配指定的ANT表达式。使用 "?" 匹配单个子路径中的单个字符，"*" 匹配单个子路径中的任意个字符，"**" 匹配任意个子路径。
         *
         * 这个实现的耗时约为基于正则时的一半。
         */
        fun matches(input: String, pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
            if (pattern.isEmpty() && input.isNotEmpty()) return false
            if (pattern == "**") return true
            val path0 = if (trimSeparator) input.trimFast('/') else input
            val pattern0 = if (trimSeparator) pattern.trimFast('/') else pattern
            if (path0.isEmpty() && (pattern0 == "**" || pattern0.all { it == '*' })) return true
            val patTokens = if (pattern0.isEmpty()) emptyList() else pattern0.splitFast('/')
            val pathTokens = if (path0.isEmpty()) emptyList() else path0.splitFast('/')
            fun match(pi: Int, si: Int): Boolean {
                var p = pi
                var s = si
                while (p < patTokens.size && s < pathTokens.size) {
                    when {
                        patTokens[p] == "**" -> {
                            if (match(p + 1, s)) return true
                            if (match(p, s + 1)) return true
                            return false
                        }
                        segmentMatchAnt(patTokens[p], pathTokens[s], ignoreCase) -> {
                            p++; s++
                        }
                        else -> return false
                    }
                }
                while (p < patTokens.size && patTokens[p] == "**") p++
                return p == patTokens.size && s == pathTokens.size
            }
            return match(0, 0)
        }

        private fun segmentMatchAnt(pattern: String, str: String, ignoreCase: Boolean): Boolean {
            var p = 0
            var s = 0
            var star = -1
            var match = 0
            val pat = if (ignoreCase) pattern.lowercase() else pattern
            val st = if (ignoreCase) str.lowercase() else str
            while (s < st.length) {
                when {
                    p < pat.length && (pat[p] == '?' || pat[p] == st[s]) -> {
                        p++; s++
                    }
                    p < pat.length && pat[p] == '*' -> {
                        star = p++; match = s
                    }
                    star != -1 -> {
                        p = star + 1; s = ++match
                    }
                    else -> return false
                }
            }
            while (p < pat.length && pat[p] == '*') p++
            return p == pat.length
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
}
