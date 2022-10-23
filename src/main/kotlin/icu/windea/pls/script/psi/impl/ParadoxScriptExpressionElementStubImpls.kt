package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyKeyStubImpl(
	parent: StubElement<*>,
	override val complexEnumValueInfo: ParadoxComplexEnumValueInfo?,
	override val gameType: ParadoxGameType?
) : StubBase<ParadoxScriptPropertyKey>(parent, ParadoxScriptStubElementTypes.PROPERTY_KEY), ParadoxScriptPropertyKeyStub {
	override fun toString(): String {
		return "ParadoxScriptPropertyKeyStub(complexEnumInfo=$complexEnumValueInfo, gameType=$gameType)"
	}
}

class ParadoxScriptStringStubImpl(
	parent: StubElement<*>,
	override val complexEnumValueInfo: ParadoxComplexEnumValueInfo?,
	override val valueSetValueInfo: ParadoxValueSetValueInfo?,
	override val gameType: ParadoxGameType?
) : StubBase<ParadoxScriptString>(parent, ParadoxScriptStubElementTypes.STRING), ParadoxScriptStringStub {
	override fun toString(): String {
		return "ParadoxScriptStringStub(complexEnumInfo=$complexEnumValueInfo, valueSetInfo=$valueSetValueInfo, gameType=$gameType)"
	}
}