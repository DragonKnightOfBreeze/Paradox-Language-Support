package icu.windea.pls.lang.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import kotlin.math.*

class ParadoxScriptExpressionLiteralTextEscaper<T : PsiLanguageInjectionHost>(
    host: T
) : LiteralTextEscaper<T>(host) {
    private var outSourceOffsets: IntArray? = null

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        val subText = rangeInsideHost.substring(myHost.text)
        outSourceOffsets = IntArray(subText.length + 1)
        return ParadoxEscapeManager.parseScriptExpressionCharacters(subText, outChars, outSourceOffsets)
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
}
