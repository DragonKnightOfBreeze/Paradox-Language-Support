package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import icu.windea.pls.core.*

/**
 * 名字中可以带有本地化命令（[ParadoxLocalisationCommand]）的 PSI 元素。
 */
interface ParadoxLocalisationCommandAwareElement: PsiElement {
    val command: ParadoxLocalisationCommand? get() = this.findChild<_>()
}
