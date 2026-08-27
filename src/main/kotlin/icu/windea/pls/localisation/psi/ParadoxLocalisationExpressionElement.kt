package icu.windea.pls.localisation.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.psi.PsiPresentableElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * @see ParadoxLocalisationCommandText
 * @see ParadoxLocalisationConceptName
 */
interface ParadoxLocalisationExpressionElement : ParadoxExpressionElement, PsiPresentableElement {
    override fun getName(): String

    override val value: String get() = text

    override fun setValue(value: String): ParadoxLocalisationExpressionElement

    override fun setContent(content: String, range: TextRange): ParadoxLocalisationExpressionElement
}
