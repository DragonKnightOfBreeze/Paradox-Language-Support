package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.config.core.config.*

interface ParadoxScriptStringExpressionElementStub<T : ParadoxScriptStringExpressionElement> : StubElement<T> {
	val complexEnumValueInfo: ParadoxComplexEnumValueInfo?
	val gameType: ParadoxGameType?
}
