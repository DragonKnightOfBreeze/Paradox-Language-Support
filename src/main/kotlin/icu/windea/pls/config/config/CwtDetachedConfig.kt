package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*

sealed interface CwtDetachedConfig<out T: PsiElement> : CwtConfig<T> {
    override val pointer: SmartPsiElementPointer<out T> get() = emptyPointer()
    override val info: CwtConfigGroupInfo get() = throw UnsupportedOperationException()
}
