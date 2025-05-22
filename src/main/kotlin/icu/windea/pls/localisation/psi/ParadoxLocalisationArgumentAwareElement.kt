package icu.windea.pls.localisation.psi

import com.intellij.psi.*

/**
 * @see ParadoxLocalisationParameter
 * @see ParadoxLocalisationCommand
 * @see ParadoxLocalisationIcon
 */
interface ParadoxLocalisationArgumentAwareElement: PsiElement {
    val argumentElement: ParadoxLocalisationArgument?
}
