package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*

interface ParadoxScriptPropertyStub: StubElement<ParadoxScriptProperty> {
	val name:String
	val typeKey:String
	val type:String
	val subtypes:List<String>
}

