package icu.windea.pls.core.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*

interface ParadoxExpressionAwareElementStub<T : ParadoxExpressionAwareElement> : StubElement<T> {
	val complexEnumValueInfo: ParadoxComplexEnumValueInfo?
	val gameType: ParadoxGameType?
}
