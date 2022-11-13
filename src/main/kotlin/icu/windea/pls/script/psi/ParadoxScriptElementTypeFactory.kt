package icu.windea.pls.script.psi

import com.intellij.psi.tree.*

object ParadoxScriptElementTypeFactory {
	@JvmStatic fun getTokenType(debugName: String): IElementType {
		return ParadoxScriptTokenType(debugName)
	}
	
	@JvmStatic fun getElementType(debugName: String): IElementType {
		return when(debugName) {
			"SCRIPTED_VARIABLE" -> ParadoxScriptStubElementTypes.SCRIPTED_VARIABLE
			"PROPERTY" -> ParadoxScriptStubElementTypes.PROPERTY
			"PROPERTY_KEY" -> ParadoxScriptStubElementTypes.PROPERTY_KEY
			"STRING" -> ParadoxScriptStubElementTypes.STRING
			else -> ParadoxScriptElementType(debugName)
		}
	}
}