@file:Suppress("unused")

package icu.windea.pls.core.text

/**
 * @see EscapePattern
 */
object EscapePatterns {
    /**
     * 默认的转义模式在转义时仅处理特殊空白字符，在反转义时处理任何字符。
     */
    object Default : EscapePattern.Base() {
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

        override fun unescape(text: String): String {
            // optimize: build string only if necessary
            var builder: StringBuilder? = null
            var index = -1
            val length = text.length
            while (index < length - 1) {
                val c = text[++index]

                if (c == '\\') {
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
     * 这个转义模式在转义时将（转义前的）特殊空白字符对应地替换为 `&nbsp;` `&emsp;` 或 `<br>`。目前不支持反转义。
     */
    object HtmlLineBreak: EscapePattern.Base() {
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

        override fun unescape(text: String): String {
            throw UnsupportedOperationException()
        }
    }
}
