package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyKeyStubImpl(
	parent: StubElement<*>,
	override val inlineScriptInfo: ParadoxInlineScriptInfo?,
	override val gameType: ParadoxGameType?
) : StubBase<ParadoxScriptPropertyKey>(parent, ParadoxScriptStubElementTypes.PROPERTY_KEY), ParadoxScriptPropertyKeyStub {
	override fun toString(): String {
		return "ParadoxScriptPropertyKeyStub(inlineScriptInfo=$inlineScriptInfo, gameType=$gameType)"
	}
}
