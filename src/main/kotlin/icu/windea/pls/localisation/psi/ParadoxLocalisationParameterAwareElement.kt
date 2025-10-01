package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import icu.windea.pls.core.*

/**
 * 可以带有本地化参数（[ParadoxLocalisationParameter]）的 PSI 元素。
 *
 * 注意：目前不支持组合使用参数与字面量的情况，相关支持尚不完善。
 */
interface ParadoxLocalisationParameterAwareElement : PsiElement {
    val parameterList: List<ParadoxLocalisationParameter> get() = this.findChildren<_>()
}
