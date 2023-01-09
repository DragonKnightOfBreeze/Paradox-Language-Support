package icu.windea.pls.script.psi

import icu.windea.pls.config.core.config.*

interface ParadoxScriptStringStub : ParadoxScriptStringExpressionElementStub<ParadoxScriptString> {
	val valueSetValueInfo: ParadoxValueSetValueInfo?
}