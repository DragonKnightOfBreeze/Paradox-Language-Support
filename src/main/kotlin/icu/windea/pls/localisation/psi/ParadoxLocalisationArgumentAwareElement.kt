package icu.windea.pls.localisation.psi

import com.intellij.psi.PsiElement

/**
 * 可以带有本地化传入参数（[ParadoxLocalisationArgument]）的 PSI 元素。
 *
 * @see ParadoxLocalisationParameter
 * @see ParadoxLocalisationCommand
 * @see ParadoxLocalisationIcon
 */
interface ParadoxLocalisationArgumentAwareElement: PsiElement {
    val argumentElement: ParadoxLocalisationArgument?
}
