package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyStubImpl(
	parent: StubElement<*>,
	override val name: String,
	override val type: String
) : StubBase<ParadoxScriptProperty>(parent, ParadoxScriptStubElementTypes.PROPERTY), ParadoxScriptPropertyStub{
	override fun toString(): String {
		return "ParadoxScriptPropertyStub(name=$name, type=$type)"
	}
}

