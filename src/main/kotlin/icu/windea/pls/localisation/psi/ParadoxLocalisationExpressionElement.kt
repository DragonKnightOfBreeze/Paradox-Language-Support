package icu.windea.pls.localisation.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * @see ParadoxLocalisationCommandText
 * @see ParadoxLocalisationConceptName
 */
interface ParadoxLocalisationExpressionElement : ParadoxExpressionElement {
    override fun setValue(value: String): ParadoxLocalisationExpressionElement

    override fun setContent(content: String, range: TextRange): ParadoxLocalisationExpressionElement
}
