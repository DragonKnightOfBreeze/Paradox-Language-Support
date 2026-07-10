package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import icu.windea.pls.core.*

/**
 * 名字中可以包含本地化命令（[ParadoxLocalisationCommand]）的 PSI 元素。
 *
 * 备注：支持包含多个本地化参数的情况，以及与普通的标识符词元组合使用的情况。
 *
 * @see ParadoxLocalisationParameter
 * @see ParadoxLocalisationIcon
 * @see ParadoxLocalisationTextIcon
 * @see ParadoxLocalisationTextFormat
 */
interface ParadoxLocalisationCommandAwareElement : PsiElement {
    val command: ParadoxLocalisationCommand? get() = this.findChild<_>()
}
