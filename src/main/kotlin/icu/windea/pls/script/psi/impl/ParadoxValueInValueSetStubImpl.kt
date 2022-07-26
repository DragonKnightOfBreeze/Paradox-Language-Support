package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxValueInValueSetStubImpl(
	parent: StubElement<*>,
	override val name: String,
	override val valueSetName: String
): StubBase<ParadoxScriptString>(parent, ParadoxScriptStubElementTypes.VALUE), ParadoxValueInValueSetStub {
	override val text: String get() = name
	
	override fun toString(): String {
		return "ParadoxValueInValueSetStub: (name=$name, valueSetName=$valueSetName)"
	}
}