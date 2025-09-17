package icu.windea.pls.config.config

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.emptyPointer

/**
 * 脱离 PSI 的规则的统一抽象。
 *
 * 概述：
 * - 与常规 [CwtConfig] 不同，脱离型规则不再依赖具体 PSI 元素与规则组（`configGroup`）。
 * - 多用于“仅计算/过渡态”的轻量结构（如独立选项项或解析/推断的中间值）。
 *
 * 注意：
 * - [pointer] 始终返回空指针（不指向任何 PSI）。
 * - [configGroup]、[getUserData]、[putUserData] 在该类型下均不支持，调用会抛出 `UnsupportedOperationException`。
 * - 如需与 PSI/规则组关联，请优先选择常规子类型（如 [CwtPropertyConfig]、[CwtValueConfig]）。
 */
interface CwtDetachedConfig : CwtConfig<PsiElement> {
    override val pointer: SmartPsiElementPointer<out PsiElement> get() = emptyPointer()
    override val configGroup: CwtConfigGroup get() = throw UnsupportedOperationException()

    override fun <T : Any?> getUserData(key: Key<T>) = throw UnsupportedOperationException()
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = throw UnsupportedOperationException()
}
