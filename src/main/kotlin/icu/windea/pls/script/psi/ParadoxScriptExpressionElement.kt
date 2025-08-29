package icu.windea.pls.script.psi

import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptValue
 */
interface ParadoxScriptExpressionElement : ParadoxExpressionElement {
    override fun setValue(value: String): ParadoxScriptExpressionElement
}
