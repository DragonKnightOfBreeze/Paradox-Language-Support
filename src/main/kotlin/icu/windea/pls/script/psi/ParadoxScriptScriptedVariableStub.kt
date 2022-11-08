package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*

interface ParadoxScriptScriptedVariableStub : StubElement<ParadoxScriptScriptedVariable> {
	val name: String?
	val gameType: ParadoxGameType?
}

