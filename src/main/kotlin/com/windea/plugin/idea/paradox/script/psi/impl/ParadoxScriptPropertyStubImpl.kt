package com.windea.plugin.idea.paradox.script.psi.impl

import com.intellij.psi.stubs.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptPropertyStubImpl(
	parent: StubElement<*>,
	override val key: String
) : StubBase<ParadoxScriptProperty>(parent, ParadoxScriptStubElementTypes.PROPERTY), ParadoxScriptPropertyStub{
	override fun toString(): String {
		return "ParadoxScriptPropertyStub: $key"
	}
}

