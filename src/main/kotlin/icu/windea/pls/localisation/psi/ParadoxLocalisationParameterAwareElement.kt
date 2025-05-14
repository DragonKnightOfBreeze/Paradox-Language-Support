package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import icu.windea.pls.core.*

/**
 * 表示此PSI可以带有本地化参数（[ParadoxLocalisationPropertyReference]）。
 *
 * 目前，不支持组合使用参数与字面量的情况，相关支持尚不完善。
 *
 * @see ParadoxLocalisationPropertyReference
 */
interface ParadoxLocalisationParameterAwareElement : PsiElement {
    val referenceElement: ParadoxLocalisationPropertyReference? get() = this.findChild<_>()

    //val parameters: List<ParadoxLocalisationPropertyReference> get() = this.findChildren<_>()
}
