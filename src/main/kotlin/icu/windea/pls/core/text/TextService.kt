package icu.windea.pls.core.text

import com.intellij.psi.LiteralTextEscaper
import java.util.*
import java.util.function.IntUnaryOperator

object TextService {
    fun convertToBooleanFromYesNo(text: String): Boolean? {
        return when (text) {
            "yes" -> true
            "no" -> false
            else -> null
        }
    }

    fun convertToBooleanLenient(text: String): Boolean? {
        return when {
            text.equals("yes", true) || text.equals("true", true) || text.equals("on", true) -> true
            text.equals("no", true) || text.equals("false", true) || text.equals("off", true) -> false
            else -> null
        }
    }

    /**
     * @see LiteralTextEscaper
     */
    fun decodeLiteralText(chars: String, out: StringBuilder, sourceOffsets: IntArray?): Boolean {
        if (chars.none { c -> c == '\\' }) {
            if (sourceOffsets != null) Arrays.setAll(sourceOffsets, IntUnaryOperator.identity())
            out.append(chars)
            return true
        }

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
            when (val c1 = chars[index++]) {
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
