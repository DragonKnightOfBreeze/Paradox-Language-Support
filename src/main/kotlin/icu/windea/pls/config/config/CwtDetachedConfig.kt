package icu.windea.pls.config.config

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.emptyPointer

interface CwtDetachedConfig : CwtConfig<PsiElement> {
    override val pointer: SmartPsiElementPointer<out PsiElement> get() = emptyPointer()
    override val configGroup: CwtConfigGroup get() = throw UnsupportedOperationException()

    override fun <T : Any?> getUserData(key: Key<T>): T? {
        throw UnsupportedOperationException()
    }

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        throw UnsupportedOperationException()
    }
}
