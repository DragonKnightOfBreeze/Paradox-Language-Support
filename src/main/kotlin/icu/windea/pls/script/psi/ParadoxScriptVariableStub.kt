package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*

interface ParadoxScriptVariableStub : StubElement<ParadoxScriptVariable> {
	val name: String?
	val gameType: ParadoxGameType?
}

