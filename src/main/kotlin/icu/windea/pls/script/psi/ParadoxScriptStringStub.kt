package icu.windea.pls.script.psi

import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*

interface ParadoxScriptStringStub : ParadoxExpressionAwareElementStub<ParadoxScriptString> {
	val valueSetValueInfo: ParadoxValueSetValueInfo?
}