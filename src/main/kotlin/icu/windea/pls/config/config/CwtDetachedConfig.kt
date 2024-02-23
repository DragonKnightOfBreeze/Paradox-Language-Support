package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*

interface CwtDetachedConfig : CwtConfig<PsiElement> {
    override val pointer: SmartPsiElementPointer<PsiElement> get() = emptyPointer()
    override val info: CwtConfigGroupInfo get() = throw UnsupportedOperationException()
}
