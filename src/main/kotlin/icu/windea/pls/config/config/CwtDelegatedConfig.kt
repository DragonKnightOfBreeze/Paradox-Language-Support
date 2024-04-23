package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*

interface CwtDelegatedConfig<out T : PsiElement, out C : CwtConfig<T>> : CwtConfig<T> {
    override val pointer: SmartPsiElementPointer<out T> get() = config.pointer
    override val configGroup: CwtConfigGroup get() = config.configGroup
    
    val config: C
}
