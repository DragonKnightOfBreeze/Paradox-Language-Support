package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

class ParadoxScriptStringStubImpl(
	parent: StubElement<*>,
	override val valueSetValueInfo: ParadoxValueSetValueInfo?
) : StubBase<ParadoxScriptString>(parent, ParadoxScriptStubElementTypes.STRING), ParadoxScriptStringStub {
	override fun toString(): String {
		val a = 1.1
		return "ParadoxScriptStringStub(valueSetInfo=$valueSetValueInfo)"
	}
}