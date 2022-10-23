package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*

interface ParadoxScriptExpressionElementStub<T : ParadoxScriptExpressionElement> : StubElement<T> {
	val complexEnumValueInfo: ParadoxComplexEnumValueInfo?
	val gameType: ParadoxGameType?
}

interface ParadoxScriptPropertyKeyStub : ParadoxScriptExpressionElementStub<ParadoxScriptPropertyKey>

interface ParadoxScriptStringStub : ParadoxScriptExpressionElementStub<ParadoxScriptString> {
	val valueSetValueInfo: ParadoxValueSetValueInfo?
}