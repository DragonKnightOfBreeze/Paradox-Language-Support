package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*

interface ParadoxScriptValueStub : StubElement<ParadoxScriptString> {
	val text: String
	val flag: Byte
}