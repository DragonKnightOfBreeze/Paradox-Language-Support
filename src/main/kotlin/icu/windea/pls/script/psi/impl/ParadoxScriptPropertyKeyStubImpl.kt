package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyKeyStubImpl(
	parent: StubElement<*>,
	override val complexEnumValueInfo: ParadoxComplexEnumValueInfo?,
	override val valueSetValueInfo: ParadoxValueSetValueInfo?,
	override val inlineScriptInfo: ParadoxInlineScriptInfo?,
	override val gameType: ParadoxGameType?
) : StubBase<ParadoxScriptPropertyKey>(parent, ParadoxScriptStubElementTypes.PROPERTY_KEY), ParadoxScriptPropertyKeyStub {
	override fun toString(): String {
		return "ParadoxScriptPropertyKeyStub(complexEnumInfo=$complexEnumValueInfo, valueSetValueInfo=$valueSetValueInfo, inlineScriptInfo=$inlineScriptInfo, gameType=$gameType)"
	}
}
