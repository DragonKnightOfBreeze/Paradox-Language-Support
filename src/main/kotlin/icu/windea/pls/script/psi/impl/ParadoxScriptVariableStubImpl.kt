package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxScriptVariableStubImpl(
	parent: StubElement<*>,
	override val name: String?,
	override val gameType: ParadoxGameType
) : StubBase<ParadoxScriptVariable>(parent, ParadoxScriptStubElementTypes.VARIABLE), ParadoxScriptVariableStub{
	override fun toString(): String {
		return "ParadoxScriptVariableStub: (name=$name, gameType=$gameType)"
	}
}

