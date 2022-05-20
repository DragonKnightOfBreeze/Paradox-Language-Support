package icu.windea.pls.script.psi

import com.intellij.psi.tree.*

object ParadoxScriptElementTypeFactory {
	@JvmStatic fun getTokenType(debugName: String): IElementType {
		return ParadoxScriptTokenType(debugName)
		//return when(debugName) {
		//	"STRING_LIKE_TOKEN" -> ParadoxScriptStringLikeTokenType
		//	else -> ParadoxScriptTokenType(debugName)
		//}
	}
	
	@JvmStatic fun getElementType(debugName: String): IElementType {
		return when(debugName) {
			"VARIABLE" -> ParadoxScriptStubElementTypes.VARIABLE
			"PROPERTY" -> ParadoxScriptStubElementTypes.PROPERTY
			//"BLOCK" -> ParadoxScriptLazyParseableElementType("BLOCK")
			else -> ParadoxScriptElementType(debugName)
		}
	}
}