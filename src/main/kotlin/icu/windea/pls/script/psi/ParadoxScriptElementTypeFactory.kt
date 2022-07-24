package icu.windea.pls.script.psi

import com.intellij.psi.tree.*

object ParadoxScriptElementTypeFactory {
	@JvmStatic fun getTokenType(debugName: String): IElementType {
		return ParadoxScriptTokenType(debugName)
	}
	
	@JvmStatic fun getElementType(debugName: String): IElementType {
		return when(debugName) {
			"VARIABLE" -> ParadoxScriptStubElementTypes.VARIABLE
			"PROPERTY" -> ParadoxScriptStubElementTypes.PROPERTY
			"STRING" -> ParadoxScriptStubElementTypes.VALUE
			else -> ParadoxScriptElementType(debugName)
		}
	}
}