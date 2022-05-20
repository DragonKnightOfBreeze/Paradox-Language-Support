package icu.windea.pls.script.psi

@Suppress("UNUSED_PARAMETER")
interface ParadoxScriptStubElementTypes {
	companion object {
		@JvmField val FILE = ParadoxScriptFileStubElementType
		@JvmField val PROPERTY = ParadoxScriptPropertyStubElementType
		@JvmField val VARIABLE = ParadoxScriptVariableStubElementType
	}
}
