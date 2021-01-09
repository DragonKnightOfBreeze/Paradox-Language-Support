package com.windea.plugin.idea.paradox.script.psi

import com.intellij.psi.stubs.*

interface ParadoxScriptVariableStub: StubElement<ParadoxScriptVariable> {
	val key:String
}
