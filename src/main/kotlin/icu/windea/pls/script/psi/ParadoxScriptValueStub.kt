package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*

interface ParadoxScriptValueStub : StubElement<ParadoxScriptValue> {
	val valueSetValueInfo: ParadoxValueSetValueInfo?
}