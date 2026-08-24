@file:Suppress("unused")

package icu.windea.pls.core.text

import icu.windea.pls.core.annotations.Optimized

/**
 * @see EscapePattern
 */
object EscapePatterns {
    /**
     * 默认的转义模式在转义时仅处理特殊空白字符，在反转义时处理任何字符。
     */
    object Default : EscapePattern.Base() {
        @Optimized
        override fun escape(text: String): String {
            // optimize: build string only if necessary
            var builder: StringBuilder? = null
            var index = -1
            val length = text.length
            while (index < length - 1) {
                val c = text[++index]

                val r = when (c) {
                    '\n' -> "\\n"
                    '\r' -> "\\r"
                    '\t' -> "\\t"
                    '\\' -> "\\\\" // also for backslash itself
                    else -> null
                }
                if (r != null) {
                    if (builder == null) builder = StringBuilder(text.substring(0, index))
                    builder.append(r)
                    continue
                }

                builder?.append(c)
            }
            return builder?.toString() ?: text
        }

        @Optimized
        override fun unescape(text: String): String {
            // optimize: build string only if necessary
            var builder: StringBuilder? = null
            var index = -1
            val length = text.length
            while (index < length - 1) {
                val c = text[++index]

                if (c == '\\') {
                    if (index == length - 1) {
                        // 末尾的孤立反斜线保持不变
                        if (builder == null) builder = StringBuilder(text.substring(0, index))
                        builder.append(c)
                        continue
                    }
                    val nc = text[++index]
                    val r = when (nc) {
                        'n' -> '\n'
                        'r' -> '\r'
                        't' -> '\t'
                        '\\' -> '\\' // also for backslash itself
                        else -> nc // also for any other character
                    }
                    if (builder == null) builder = StringBuilder(text.substring(0, index - 1))
                    builder.append(r)
                    continue
                }

                builder?.append(c)
            }
            return builder?.toString() ?: text
        }
    }

    /**
     * 这个转义模式在转义时将换行符（`\n` 或 `\r`）替换为 `<br>` 并保留其后的换行符，将制表符（`\t`）替换为 `&emsp;`，用于在 HTML 中显示文本。
     * 反转义时对应地将 `<br>`（及其后可能跟随的换行符）还原为换行符，将 `&emsp;` 还原为制表符。
     */
    object HtmlLineBreak: EscapePattern.Base() {
        @Optimized
        override fun escape(text: String): String {
            // optimize: build string only if necessary
            var builder: StringBuilder? = null
            var index = -1
            val length = text.length
            while (index < length - 1) {
                val c = text[++index]

                val r = when (c) {
                    '\n' -> "<br>\n" // keep line break
                    '\r' -> "<br>\n" // keep line break
                    '\t' -> "&emsp;"
                    // ' ' -> "&nbsp;" // not for spaces atm
                    else -> null
                }
                if (r != null) {
                    if (builder == null) builder = StringBuilder(text.substring(0, index))
                    builder.append(r)
                    continue
                }

                builder?.append(c)
            }
            return builder?.toString() ?: text
        }

        @Optimized
        override fun unescape(text: String): String {
            // optimize: build string only if necessary
            var builder: StringBuilder? = null
            var index = -1
            val length = text.length
            while (index < length - 1) {
                val c = text[++index]
                val startIndex = index

                val r = when {
                    c == '&' && text.startsWith("&emsp;", index) -> {
                        index += "&emsp;".length - 1
                        "\t"
                    }
                    c == '<' && text.startsWith("<br>", index) -> {
                        index += "<br>".length - 1
                        // 若其后紧跟换行符，也一并消费，避免残留多余的换行符
                        if (index < length - 1 && (text[index + 1] == '\n' || text[index + 1] == '\r')) {
                            index += 1
                        }
                        "\n"
                    }
                    else -> null
                }
                if (r != null) {
                    if (builder == null) builder = StringBuilder(text.substring(0, startIndex))
                    builder.append(r)
                    continue
                }

                builder?.append(c)
            }
            return builder?.toString() ?: text
        }
    }
}
