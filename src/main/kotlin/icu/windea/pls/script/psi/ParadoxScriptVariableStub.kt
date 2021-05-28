package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*

interface ParadoxScriptVariableStub: StubElement<ParadoxScriptVariable> {
	val key:String
}
