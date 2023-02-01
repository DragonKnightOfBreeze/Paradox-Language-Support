package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyKeyStubImpl(
	parent: StubElement<*>,
	override val complexEnumValueInfo: ParadoxComplexEnumValueInfo?,
	override val inlineScriptInfo: ParadoxInlineScriptInfo?,
	override val gameType: ParadoxGameType?
) : StubBase<ParadoxScriptPropertyKey>(parent, ParadoxScriptStubElementTypes.PROPERTY_KEY), ParadoxScriptPropertyKeyStub {
	override fun toString(): String {
		return "ParadoxScriptPropertyKeyStub(complexEnumInfo=$complexEnumValueInfo, gameType=$gameType)"
	}
}
