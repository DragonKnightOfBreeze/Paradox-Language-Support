package icu.windea.pls.localisation.psi

import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.psi.mock.*

/**
 * @see ParadoxLocalisationCommandText
 * @see ParadoxLocalisationConceptName
 */
interface ParadoxLocalisationExpressionElement : ParadoxExpressionElement {
    override fun setValue(value: String): ParadoxLocalisationExpressionElement
}
