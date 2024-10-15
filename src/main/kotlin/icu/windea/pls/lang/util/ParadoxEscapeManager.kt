package icu.windea.pls.lang.util

import java.util.*
import java.util.function.*

object ParadoxEscapeManager {
    enum class Type {
        Default, Html, Inlay
    }

    fun unescapeScriptExpression(value: String, builder: StringBuilder, type: Type = Type.Default) {
        var isEscape = false
        value.forEach f@{ c ->
            if (isEscape) {
                isEscape = false
                when (c) {
                    'n' -> {
                        when (type) {
                            Type.Html -> builder.append("<br>\n")
                            Type.Inlay -> return //内嵌提示不能换行，因此这里需要直接截断
                            else -> builder.append('\n')
                        }
                    }
                    'r' -> {
                        when (type) {
                            Type.Html -> builder.append("<br>\r")
                            Type.Inlay -> return //内嵌提示不能换行，因此这里需要直接截断
                            else -> builder.append('\r')
                        }
                    }
                    't' -> {
                        when (type) {
                            Type.Html -> builder.append("&emsp;")
                            else -> builder.append('\t')
                        }
                    }
                    else -> {
                        builder.append(c)
                    }
                }
                return@f
            }
            when {
                c == '\\' -> isEscape = true
                else -> builder.append(c)
            }
        }
    }

    fun unescapeLocalisationString(value: String, builder: StringBuilder, type: Type = Type.Default) {
        var isEscape = false
        var isLeftBracket = false
        value.forEach f@{ c ->
            if (isLeftBracket) {
                isLeftBracket = false
                if (c == '[') {
                    builder.append('[')
                    return@f
                } else {
                    builder.append('[')
                }
            }
            if (isEscape) {
                isEscape = false
                when (c) {
                    'n' -> {
                        when (type) {
                            Type.Html -> builder.append("<br>\n")
                            Type.Inlay -> return //内嵌提示不能换行，因此这里需要直接截断
                            else -> builder.append('\n')
                        }
                    }
                    'r' -> {
                        when (type) {
                            Type.Html -> builder.append("<br>\r")
                            Type.Inlay -> return //内嵌提示不能换行，因此这里需要直接截断
                            else -> builder.append('\r')
                        }
                    }
                    't' -> {
                        when (type) {
                            Type.Html -> builder.append("&emsp;")
                            else -> builder.append('\t')
                        }
                    }
                    else -> {
                        builder.append(c)
                    }
                }
                return@f
            }
            when {
                c == '\\' -> isEscape = true
                c == '[' -> isLeftBracket = true
                else -> builder.append(c)
            }
        }
    }

    fun parseScriptExpressionCharacters(chars: String, out: StringBuilder, sourceOffsets: IntArray?): Boolean {
        if (chars.none { c -> c == '\\' }) {
            if (sourceOffsets != null) Arrays.setAll(sourceOffsets, IntUnaryOperator.identity())
            out.append(chars)
            return true
        }
        return parseScriptExpressionCharactersWithEscape(out, chars, sourceOffsets)
    }

    private fun parseScriptExpressionCharactersWithEscape(out: StringBuilder, chars: String, sourceOffsets: IntArray?): Boolean {
        val outOffset = out.length
        var index = 0
        while (index < chars.length) {
            val c = chars[index++]
            if (sourceOffsets != null) {
                sourceOffsets[out.length - outOffset] = index - 1
                sourceOffsets[out.length + 1 - outOffset] = index
            }
            if (c != '\\') {
                out.append(c)
                continue
            }
            if (index == chars.length) return false
            val c1 = chars[index++]
            when (c1) {
                '"' -> {
                    out.append('"')
                    if (sourceOffsets != null) {
                        sourceOffsets[out.length - outOffset] = index
                    }
                }
                '\\' -> {
                    out.append('\\')
                    if (sourceOffsets != null) {
                        sourceOffsets[out.length - outOffset] = index
                    }
                }
                else -> {
                    out.append('\\').append(c1)
                }
            }
        }
        return true
    }
}
