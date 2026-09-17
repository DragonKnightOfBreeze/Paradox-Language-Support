package icu.windea.pls.script.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * 可以作为表达式的 [PsiElement]。
 *
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptValue
 */
interface ParadoxScriptExpressionElement : ParadoxExpressionElement {
    override fun getName(): String

    override val value: String get() = text

    override fun setValue(value: String): ParadoxScriptExpressionElement

    override fun setContent(content: String, range: TextRange): ParadoxScriptExpressionElement
}
