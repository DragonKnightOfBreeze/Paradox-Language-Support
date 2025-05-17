package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import icu.windea.pls.core.*

/**
 * 表示此PSI可以带有本地化命令（[ParadoxLocalisationCommand]），将其整个作为名字。
 */
interface ParadoxLocalisationCommandAwareElement: PsiElement {
    val command: ParadoxLocalisationCommand? get() = this.findChild<_>()
}
