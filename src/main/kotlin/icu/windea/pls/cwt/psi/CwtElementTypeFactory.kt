package icu.windea.pls.cwt.psi

import com.intellij.psi.tree.IElementType

object CwtElementTypeFactory {
    @JvmStatic
    fun getTokenType(debugName: String): IElementType {
        return CwtTokenType(debugName)
    }

    @JvmStatic
    fun getElementType(debugName: String): IElementType {
        return CwtElementType(debugName)
    }
}
