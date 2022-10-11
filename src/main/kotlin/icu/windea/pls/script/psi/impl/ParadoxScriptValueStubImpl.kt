package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueStubImpl(
	parent: StubElement<*>,
	override val valueSetValueInfo: ParadoxValueSetValueInfo?
) : StubBase<ParadoxScriptValue>(parent, ParadoxScriptStubElementTypes.VALUE), ParadoxScriptValueStub {
	override fun toString(): String {
		return "ParadoxScriptValueStub(valueSetInfo=$valueSetValueInfo)"
	}
}