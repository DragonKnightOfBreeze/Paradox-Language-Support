package icu.windea.pls.localisation.psi

import icu.windea.pls.lang.psi.*

/**
 * @see ParadoxLocalisationConceptName
 */
interface ParadoxLocalisationExpressionElement: ParadoxExpressionElement {
    override fun setValue(value: String): ParadoxLocalisationExpressionElement
}