package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.model.*

interface ParadoxScriptScriptedVariableStub : StubElement<ParadoxScriptScriptedVariable> {
	val name: String?
	val gameType: ParadoxGameType?
}

