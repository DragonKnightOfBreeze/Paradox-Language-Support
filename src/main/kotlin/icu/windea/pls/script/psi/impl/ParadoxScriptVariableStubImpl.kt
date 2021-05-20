package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxScriptVariableStubImpl(
	parent: StubElement<*>,
	override val key: String
) : StubBase<ParadoxScriptVariable>(parent, ParadoxScriptStubElementTypes.VARIABLE), ParadoxScriptVariableStub{
	override fun toString(): String {
		return "ParadoxScriptVariableStub: (key=$key)"
	}
}

