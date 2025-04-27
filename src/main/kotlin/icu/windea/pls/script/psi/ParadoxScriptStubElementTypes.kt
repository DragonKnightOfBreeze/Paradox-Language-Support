package icu.windea.pls.script.psi

interface ParadoxScriptStubElementTypes {
    companion object {
        @JvmField
        val FILE = ParadoxScriptFileStubElementType.INSTANCE
        @JvmField
        val SCRIPTED_VARIABLE = ParadoxScriptScriptedVariableStubElementType.INSTANCE
        @JvmField
        val PROPERTY = ParadoxScriptPropertyStubElementType.INSTANCE
    }
}
