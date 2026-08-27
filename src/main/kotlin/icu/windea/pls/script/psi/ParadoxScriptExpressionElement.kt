package icu.windea.pls.script.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.psi.PsiPresentableElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptValue
 */
interface ParadoxScriptExpressionElement : ParadoxExpressionElement, PsiPresentableElement {
    override fun getName(): String

    override val value: String get() = text

    override fun setValue(value: String): ParadoxScriptExpressionElement

    override fun setContent(content: String, range: TextRange): ParadoxScriptExpressionElement
}
