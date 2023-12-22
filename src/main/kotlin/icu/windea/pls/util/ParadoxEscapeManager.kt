package icu.windea.pls.util

import icu.windea.pls.core.collections.*
import java.lang.StringBuilder

object ParadoxEscapeManager {
    enum class Type {
        Default, Html, Inlay
    }
    
    fun escapeScriptExpression(value: String, builder: StringBuilder, type: Type = Type.Default) {
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
    
    fun escapeLocalisationString(value: String, builder: StringBuilder, type: Type = Type.Default) {
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
}