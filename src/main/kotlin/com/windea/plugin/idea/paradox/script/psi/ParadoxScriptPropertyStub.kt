package com.windea.plugin.idea.paradox.script.psi

import com.intellij.psi.stubs.*

interface ParadoxScriptPropertyStub: StubElement<ParadoxScriptProperty> {
	val name:String
	val type:String
}

