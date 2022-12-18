package icu.windea.pls.script.psi

import icu.windea.pls.core.model.*

interface ParadoxScriptStringStub : ParadoxScriptStringExpressionElementStub<ParadoxScriptString> {
	val valueSetValueInfo: ParadoxValueSetValueInfo?
}