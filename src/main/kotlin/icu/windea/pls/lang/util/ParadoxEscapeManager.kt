package icu.windea.pls.lang.util

import icu.windea.pls.core.text.EscapeType

@Suppress("unused")
object ParadoxEscapeManager {
    fun unescapeScriptText(value: String, builder: StringBuilder, type: EscapeType = EscapeType.Default) {
        var isEscape = false
        for (c in value) {
            if (isEscape) {
                isEscape = false
                when (c) {
                    'n' -> {
                        when (type) {
                            EscapeType.Html -> builder.append("<br>\n")
                            EscapeType.Inlay -> return // 内嵌提示不能换行，因此这里需要直接截断
                            else -> builder.append('\n')
                        }
                    }
                    'r' -> {
                        when (type) {
                            EscapeType.Html -> builder.append("<br>\r")
                            EscapeType.Inlay -> return // 内嵌提示不能换行，因此这里需要直接截断
                            else -> builder.append('\r')
                        }
                    }
                    't' -> {
                        when (type) {
                            EscapeType.Html -> builder.append("&emsp;")
                            else -> builder.append('\t')
                        }
                    }
                    else -> {
                        builder.append(c)
                    }
                }
                continue
            }
            when (c) {
                '\\' -> isEscape = true
                else -> builder.append(c)
            }
        }
    }

    fun unescapeScriptText(value: String, type: EscapeType = EscapeType.Default): String {
        return buildString { unescapeScriptText(value, this, type) }
    }

    fun unescapeLocalisationText(value: String, builder: StringBuilder, type: EscapeType = EscapeType.Default) {
        var isEscape = false
        var isLeftBracket = false
        for (c in value) {
            if (isLeftBracket) {
                isLeftBracket = false
                if (c == '[') {
                    builder.append('[')
                    continue
                } else {
                    builder.append('[')
                }
            }
            if (isEscape) {
                isEscape = false
                when (c) {
                    'n' -> {
                        when (type) {
                            EscapeType.Html -> builder.append("<br>\n")
                            EscapeType.Inlay -> return // 内嵌提示不能换行，因此这里需要直接截断
                            else -> builder.append('\n')
                        }
                    }
                    'r' -> {
                        when (type) {
                            EscapeType.Html -> builder.append("<br>\r")
                            EscapeType.Inlay -> return // 内嵌提示不能换行，因此这里需要直接截断
                            else -> builder.append('\r')
                        }
                    }
                    't' -> {
                        when (type) {
                            EscapeType.Html -> builder.append("&emsp;")
                            else -> builder.append('\t')
                        }
                    }
                    else -> {
                        builder.append(c)
                    }
                }
                continue
            }
            when (c) {
                '\\' -> isEscape = true
                '[' -> isLeftBracket = true
                else -> builder.append(c)
            }
        }
    }

    fun unescapeLocalisationText(value: String, type: EscapeType = EscapeType.Default): String {
        return buildString { unescapeLocalisationText(value, this, type) }
    }
}
