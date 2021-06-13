package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxScriptVariableStubImpl(
	parent: StubElement<*>,
	override val name: String
) : StubBase<ParadoxScriptVariable>(parent, ParadoxScriptStubElementTypes.VARIABLE), ParadoxScriptVariableStub{
	override fun toString(): String {
		return "ParadoxScriptVariableStub: (key= $name)"
	}
}

