package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*

sealed interface CwtDelegatedConfig<out T : PsiElement, out C : CwtConfig<T>> : CwtConfig<T> {
    override val pointer: SmartPsiElementPointer<out T> get() = config.pointer
    override val info: CwtConfigGroupInfo get() = config.info
    
    val config: C
}
