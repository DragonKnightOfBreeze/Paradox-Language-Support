@file:Suppress("unused")

package icu.windea.pls.localisation.text

import icu.windea.pls.core.text.EscapePattern
import icu.windea.pls.core.text.EscapePatterns

@Suppress("UnusedReceiverParameter")
val EscapePatterns.ParadoxLocalisation: EscapePattern.Base get() = ParadoxLocalisationEscapePattern

private object ParadoxLocalisationEscapePattern : EscapePattern.Base() {
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

            // `[[` -> `[`
            if (c == '[' && index < length - 1 && text[index + 1] == '[') {
                if (builder == null) builder = StringBuilder(text.substring(0, index))
                builder.append('[')
                index += 1
                continue
            }

            if (c == '\\' && index < length - 1) {
                val nc = text[index + 1]
                val r = when (nc) {
                    'n' -> '\n'
                    'r' -> '\r'
                    't' -> '\t'
                    '\\' -> '\\' // also for backslash itself
                    else -> nc // also for any other character
                }
                if (builder == null) builder = StringBuilder(text.substring(0, index))
                builder.append(r)
                index += 1
                continue
            }

            builder?.append(c)
        }
        return builder?.toString() ?: text
    }
}
