package icu.windea.pls.config.config

import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configGroup.CwtConfigGroup

interface CwtDelegatedConfig<out E : PsiElement, out C : CwtConfig<E>> : CwtConfig<E> {
    override val pointer: SmartPsiElementPointer<out E> get() = config.pointer
    override val configGroup: CwtConfigGroup get() = config.configGroup

    val config: C
}
