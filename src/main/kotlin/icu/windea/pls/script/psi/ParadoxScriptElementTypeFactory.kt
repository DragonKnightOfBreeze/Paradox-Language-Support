package icu.windea.pls.script.psi

import com.intellij.psi.tree.IElementType

object ParadoxScriptElementTypeFactory {
    @JvmStatic
    fun getTokenType(debugName: String): IElementType {
        return when (debugName) {
            "INLINE_MATH_TOKEN" -> ParadoxScriptInlineMathElementType(debugName)
            else -> ParadoxScriptTokenType(debugName)
        }
    }

    @JvmStatic
    fun getElementType(debugName: String): IElementType {
        return ParadoxScriptElementType(debugName)
    }
}
