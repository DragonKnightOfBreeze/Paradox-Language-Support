package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueStubImpl(
	parent: StubElement<*>,
	override val valueSetValueInfo: ParadoxValueSetValueInfo?
) : StubBase<ParadoxScriptValue>(parent, ParadoxScriptStubElementTypes.STRING), ParadoxScriptValueStub {
	override fun toString(): String {
		val a = 1.1
		return "ParadoxScriptValueStub(valueSetInfo=$valueSetValueInfo)"
	}
}