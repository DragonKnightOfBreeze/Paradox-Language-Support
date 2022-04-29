package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyStubImpl(
	parent: StubElement<*>,
	override val name: String? = null,
	override val type: String? = null,
	override val subtypes: List<String>? = null,
	override val rootKey: String? = null
) : StubBase<ParadoxScriptProperty>(parent, ParadoxScriptStubElementTypes.PROPERTY), ParadoxScriptPropertyStub{
	override fun toString(): String {
		return "ParadoxScriptPropertyStub(name=$name, type=$type, subtypes=$subtypes, rootKey=$rootKey)"
	}
}

