package com.windea.plugin.idea.paradox.script.psi.impl

import com.intellij.psi.stubs.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptVariableStubImpl(
	parent: StubElement<*>,
	override val key: String
) : StubBase<ParadoxScriptVariable>(parent, ParadoxScriptStubElementTypes.VARIABLE), ParadoxScriptVariableStub{
	override fun toString(): String {
		return "ParadoxScriptVariableStub: $key"
	}
}

