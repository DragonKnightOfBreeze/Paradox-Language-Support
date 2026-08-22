package icu.windea.pls.script.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptValue
 */
interface ParadoxScriptExpressionElement : ParadoxExpressionElement {
    override fun setValue(value: String): ParadoxScriptExpressionElement

    override fun setContent(content: String, range: TextRange): ParadoxScriptExpressionElement
}
