package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*

interface ParadoxScriptDefinitionStub : StubElement<ParadoxScriptProperty> {
	val name: String
	val type: String
	val subtypes: List<String>
	val rootKey: String
}