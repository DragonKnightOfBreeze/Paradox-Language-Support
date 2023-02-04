package icu.windea.pls.script.psi

import icu.windea.pls.lang.model.*

interface ParadoxScriptPropertyKeyStub : ParadoxScriptStringExpressionElementStub<ParadoxScriptPropertyKey> {
    val inlineScriptInfo: ParadoxInlineScriptInfo?
}