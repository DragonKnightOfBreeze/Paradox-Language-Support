package com.windea.plugin.idea.paradox.script.psi

import com.windea.plugin.idea.paradox.script.psi.*

@Suppress("UNUSED_PARAMETER")
interface ParadoxScriptStubElementTypes {
	companion object {
		@JvmField val FILE = ParadoxScriptFileStubElementType()
		@JvmField val PROPERTY = ParadoxScriptPropertyStubElementType()
		@JvmField val VARIABLE = ParadoxScriptVariableStubElementType()
		
		@JvmStatic fun getPropertyType(name:String) = PROPERTY
		@JvmStatic fun getVariableType(name:String) = VARIABLE
	}
}
