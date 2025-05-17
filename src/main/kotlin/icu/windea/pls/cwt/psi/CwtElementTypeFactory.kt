package icu.windea.pls.cwt.psi

import com.intellij.psi.tree.*

object CwtElementTypeFactory {
    @JvmStatic
    fun getTokenType(debugName: String): IElementType {
        return when (debugName) {
            "OPTION_COMMENT_TOKEN" -> CwtOptionCommentElementType(debugName)
            else -> CwtTokenType(debugName)
        }
    }

    @JvmStatic
    fun getElementType(debugName: String): IElementType {
        return CwtElementType(debugName)
    }
}
