package icu.windea.pls.csv.psi

import com.intellij.psi.tree.*

object ParadoxCsvElementTypeFactory {
    @JvmStatic
    fun getTokenType(debugName: String): IElementType {
        return ParadoxCsvTokenType(debugName)
    }

    @JvmStatic
    fun getElementType(debugName: String): IElementType {
        return when (debugName) {
            "ROW" -> ParadoxCsvRowStubElementType.INSTANCE
            else -> ParadoxCsvElementType(debugName)
        }
    }
}
