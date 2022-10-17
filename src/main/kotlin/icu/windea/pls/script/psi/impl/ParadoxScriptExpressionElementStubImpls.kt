package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyKeyStubImpl(
	parent: StubElement<*>,
	override val complexEnumInfo: ParadoxComplexEnumInfo?,
	override val gameType: ParadoxGameType?
) : StubBase<ParadoxScriptPropertyKey>(parent, ParadoxScriptStubElementTypes.PROPERTY_KEY), ParadoxScriptPropertyKeyStub {
	override fun toString(): String {
		return "ParadoxScriptPropertyKeyStub(complexEnumInfo=$complexEnumInfo, gameType=$gameType)"
	}
}

class ParadoxScriptStringStubImpl(
	parent: StubElement<*>,
	override val complexEnumInfo: ParadoxComplexEnumInfo?,
	override val valueSetValueInfo: ParadoxValueSetValueInfo?,
	override val gameType: ParadoxGameType?
) : StubBase<ParadoxScriptString>(parent, ParadoxScriptStubElementTypes.STRING), ParadoxScriptStringStub {
	override fun toString(): String {
		return "ParadoxScriptStringStub(complexEnumInfo=$complexEnumInfo, valueSetInfo=$valueSetValueInfo, gameType=$gameType)"
	}
}