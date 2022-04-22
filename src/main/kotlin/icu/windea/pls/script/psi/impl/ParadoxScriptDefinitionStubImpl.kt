package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxScriptDefinitionStubImpl(
	parent: StubElement<*>,
	override val name: String,
	override val type: String,
	override val subtypes: List<String>,
	override val rootKey: String
) : StubBase<ParadoxScriptProperty>(parent, ParadoxScriptStubElementTypes.PROPERTY), ParadoxScriptDefinitionStub{
	override fun toString(): String {
		return "ParadoxScriptDefinitionStub(name=$name, type=$type, subtypes=$subtypes, rootKey=$rootKey)"
	}
}

