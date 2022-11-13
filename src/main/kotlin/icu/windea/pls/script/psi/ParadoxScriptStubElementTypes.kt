package icu.windea.pls.script.psi

interface ParadoxScriptStubElementTypes {
	companion object {
		@JvmField val FILE = ParadoxScriptFileStubElementType
		@JvmField val SCRIPTED_VARIABLE = ParadoxScriptScriptedVariableStubElementType
		@JvmField val PROPERTY = ParadoxScriptPropertyStubElementType
		@JvmField val PROPERTY_KEY = ParadoxScriptPropertyKeyStubElementType
		@JvmField val STRING = ParadoxScriptStringStubElementType
	}
}
