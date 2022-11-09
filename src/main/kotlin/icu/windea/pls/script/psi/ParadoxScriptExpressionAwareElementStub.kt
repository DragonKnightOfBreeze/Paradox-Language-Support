package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*

interface ParadoxScriptExpressionElementStub<T : ParadoxExpressionElement> : StubElement<T> {
	val complexEnumValueInfo: ParadoxComplexEnumValueInfo?
	val gameType: ParadoxGameType?
}
