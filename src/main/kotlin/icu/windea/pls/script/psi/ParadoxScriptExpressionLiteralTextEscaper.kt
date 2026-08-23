package icu.windea.pls.script.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import java.util.*
import java.util.function.IntUnaryOperator
import kotlin.math.min

class ParadoxScriptExpressionLiteralTextEscaper<T : PsiLanguageInjectionHost>(
    host: T
) : LiteralTextEscaper<T>(host) {
    private var outSourceOffsets: IntArray? = null

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        val subText = rangeInsideHost.substring(myHost.text)
        outSourceOffsets = IntArray(subText.length + 1)
        return parseCharacters(subText, outChars, outSourceOffsets)
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        val outSourceOffsets = outSourceOffsets!!
        val result = if (offsetInDecoded < outSourceOffsets.size) outSourceOffsets[offsetInDecoded] else -1
        if (result == -1) return -1
        return min(result, rangeInsideHost.length) + rangeInsideHost.startOffset
    }

    override fun isOneLine(): Boolean {
        return myHost is ParadoxScriptPropertyKey || myHost is ParadoxParameter
    }

    private fun parseCharacters(chars: String, out: StringBuilder, sourceOffsets: IntArray?): Boolean {
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
