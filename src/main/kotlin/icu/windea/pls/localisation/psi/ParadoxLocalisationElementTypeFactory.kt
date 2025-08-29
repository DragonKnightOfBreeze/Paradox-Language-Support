package icu.windea.pls.localisation.psi

import com.intellij.psi.tree.IElementType

object ParadoxLocalisationElementTypeFactory {
    @JvmStatic
    fun getTokenType(debugName: String): IElementType {
        return when (debugName) {
            "PROPERTY_VALUE_TOKEN" -> ParadoxLocalisationPropertyValueElementType(debugName)
            else -> ParadoxLocalisationTokenType(debugName)
        }
    }

    @JvmStatic
    fun getElementType(debugName: String): IElementType {
        // return when (debugName) {
        //     "PROPERTY" -> ParadoxLocalisationPropertyStubElementType.INSTANCE
        //     else -> ParadoxLocalisationElementType(debugName)
        // }
        return ParadoxLocalisationElementType(debugName)
    }
}
