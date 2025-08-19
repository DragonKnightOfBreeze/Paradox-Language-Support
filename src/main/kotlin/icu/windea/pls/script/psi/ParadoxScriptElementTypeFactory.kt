package icu.windea.pls.script.psi

import com.intellij.psi.tree.*

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
        // return when (debugName) {
        //     "SCRIPTED_VARIABLE" -> ParadoxScriptStubElementTypes.SCRIPTED_VARIABLE
        //     "PROPERTY" -> ParadoxScriptStubElementTypes.PROPERTY
        //     else -> ParadoxScriptElementType(debugName)
        // }
        return ParadoxScriptElementType(debugName)
    }
}
