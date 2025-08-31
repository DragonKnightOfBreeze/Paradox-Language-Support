package icu.windea.pls.config.config

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.emptyPointer

/**
 * CWT 脱离规则（Detached）。
 *
 * 表示不绑定到具体 PSI 的规则对象（如：仅在内存中辅助构建模型）。
 * 其 [pointer] 始终为空指针；[configGroup] 与用户数据访问不被支持。
 */
interface CwtDetachedConfig : CwtConfig<PsiElement> {
    override val pointer: SmartPsiElementPointer<out PsiElement> get() = emptyPointer()
    override val configGroup: CwtConfigGroup get() = throw UnsupportedOperationException()

    override fun <T : Any?> getUserData(key: Key<T>) = throw UnsupportedOperationException()
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = throw UnsupportedOperationException()
}
