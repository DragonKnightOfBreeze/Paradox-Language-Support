package icu.windea.pls.util

import icu.windea.pls.core.collections.*

object ParadoxEscapeManager {
    enum class Type {
        Default, Html, Inlay
    }
    
    fun escapeCwtExpression(value: String, builder: StringBuilder) {
        value.forEachFast f@{ c ->
            when(c) {
                '"' -> builder.append("\\\"")
                '\\' -> builder.append("\\\\")
                else -> builder.append(c)
            }
        }
        builder.append(value)
    }
    
    fun escapeScriptExpression(value: String, builder: StringBuilder) {
        value.forEachFast f@{ c ->
            when(c) {
                '"' -> builder.append("\\\"")
                '\\' -> builder.append("\\\\")
                else -> builder.append(c)
            }
        }
        builder.append(value)
    }
    
    fun escapeLocalisationExpression(value: String, builder: StringBuilder) {
        value.forEachFast f@{ c ->
            when(c) {
                '"' -> builder.append("\\\"")
                '\\' -> builder.append("\\\\")
                '[' -> builder.append("[[")
                else -> builder.append(c)
            }
        }
        builder.append(value)
    }
    
    fun renderScriptExpression(value: String, builder: StringBuilder, type: Type = Type.Default) {
        var isEscape = false
        value.forEachFast f@{ c ->
            if(isEscape) {
                isEscape = false
                when(c) {
                    'n' -> {
                        when(type) {
                            Type.Html -> builder.append("<br>\n")
                            Type.Inlay -> return //内嵌提示不能换行，因此这里需要直接截断
                            else -> builder.append('\n')
                        }
                    }
                    'r' -> {
                        when(type) {
                            Type.Html -> builder.append("<br>\r")
                            Type.Inlay -> return //内嵌提示不能换行，因此这里需要直接截断
                            else -> builder.append('\r')
                        }
                    }
                    't' -> {
                        when(type) {
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
    
    fun renderLocalisationString(value: String, builder: StringBuilder, type: Type = Type.Default) {
        var isEscape = false
        var isLeftBracket = false
        value.forEachFast f@{ c ->
            if(isLeftBracket) {
                isLeftBracket = false
                if(c == '[') {
                    builder.append('[')
                    return@f
                } else {
                    builder.append('[')
                }
            }
            if(isEscape) {
                isEscape = false
                when(c) {
                    'n' -> {
                        when(type) {
                            Type.Html -> builder.append("<br>\n")
                            Type.Inlay -> return //内嵌提示不能换行，因此这里需要直接截断
                            else -> builder.append('\n')
                        }
                    }
                    'r' -> {
                        when(type) {
                            Type.Html -> builder.append("<br>\r")
                            Type.Inlay -> return //内嵌提示不能换行，因此这里需要直接截断
                            else -> builder.append('\r')
                        }
                    }
                    't' -> {
                        when(type) {
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
        var index = 0
        val outOffset = out.length
        while(index < chars.length) {
            val c = chars[index++]
            if(sourceOffsets != null) {
                sourceOffsets[out.length - outOffset] = index - 1
                sourceOffsets[out.length + 1 - outOffset] = index
            }
            if(c != '\\') {
                out.append(c)
                continue
            }
            val newIndex = parseEscapedSymbolInScriptExpression(chars, index, out)
            if(index == newIndex) {
                continue
            }
            index = newIndex
            if(index == -1) return false
            if(sourceOffsets != null) {
                sourceOffsets[out.length - outOffset] = index
            }
        }
        return true
    }
    
    @Suppress("NAME_SHADOWING")
    private fun parseEscapedSymbolInScriptExpression(chars: String, index: Int, out: StringBuilder): Int {
        var index = index
        if(index == chars.length) return -1
        val c = chars[index++]
        when(c) {
            '"' -> {
                out.append('"')
            }
            '\\' -> {
                out.append('\\')
            }
            else -> {
                //no escape
                out.append('\\').append(c)
                return index - 1
            }
        }
        return index
    }
}