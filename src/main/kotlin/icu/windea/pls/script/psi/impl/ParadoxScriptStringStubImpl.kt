package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class ParadoxScriptStringStubImpl(
	parent: StubElement<*>,
	override val complexEnumValueInfo: ParadoxComplexEnumValueInfo?,
	override val gameType: ParadoxGameType?
) : StubBase<ParadoxScriptString>(parent, ParadoxScriptStubElementTypes.STRING), ParadoxScriptStringStub {
	override fun toString(): String {
		return "ParadoxScriptStringStub(complexEnumInfo=$complexEnumValueInfo, gameType=$gameType)"
	}
}