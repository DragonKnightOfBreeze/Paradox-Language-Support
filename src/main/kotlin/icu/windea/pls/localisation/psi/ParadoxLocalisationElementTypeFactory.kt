package icu.windea.pls.localisation.psi

import com.intellij.psi.tree.*

object ParadoxLocalisationElementTypeFactory {
	@JvmStatic fun getTokenType(debugName: String): IElementType {
		return when(debugName) {
			"STRING_TOKEN" -> ParadoxLocalisationStringTokenType(debugName)
			else -> ParadoxLocalisationTokenType(debugName)
		}
	}
	
	@JvmStatic fun getElementType(debugName: String): IElementType {
		return ParadoxLocalisationElementType(debugName)
	}
}