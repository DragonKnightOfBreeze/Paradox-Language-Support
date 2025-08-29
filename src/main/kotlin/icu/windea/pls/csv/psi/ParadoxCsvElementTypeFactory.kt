package icu.windea.pls.csv.psi

import com.intellij.psi.tree.IElementType

object ParadoxCsvElementTypeFactory {
    @JvmStatic
    fun getTokenType(debugName: String): IElementType {
        return ParadoxCsvTokenType(debugName)
    }

    @JvmStatic
    fun getElementType(debugName: String): IElementType {
        return ParadoxCsvElementType(debugName)
    }
}
