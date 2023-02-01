package icu.windea.pls.script.psi

import icu.windea.pls.config.core.config.*

interface ParadoxScriptPropertyKeyStub : ParadoxScriptStringExpressionElementStub<ParadoxScriptPropertyKey> {
    val inlineScriptInfo: ParadoxInlineScriptInfo?
}