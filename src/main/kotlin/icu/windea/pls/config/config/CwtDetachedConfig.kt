package icu.windea.pls.config.config

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.emptyPointer

/**
 * 脱离 PSI 的 CWT config。
 *
 * 概述：
 * - 与常规 [CwtConfig] 不同，脱离型 config 不再依赖具体 PSI 元素与规则组（`configGroup`）。
 * - 主要用于承载“仅用于计算或过渡态”的轻量结构，例如：独立的选项项（option）或在解析/推断中的中间值。
 *
 * 注意：
 * - [pointer] 始终返回一个空指针（不会指向任何 PSI）。
 * - [configGroup]、[getUserData]、[putUserData] 在该类型下一律不支持，调用会抛出 `UnsupportedOperationException`。
 * - 如果需要与 PSI/规则组关联，请优先选择常规的 [CwtConfig] 子类型（如 [CwtPropertyConfig]、[CwtValueConfig]）。
 */
interface CwtDetachedConfig : CwtConfig<PsiElement> {
    override val pointer: SmartPsiElementPointer<out PsiElement> get() = emptyPointer()
    override val configGroup: CwtConfigGroup get() = throw UnsupportedOperationException()

    override fun <T : Any?> getUserData(key: Key<T>) = throw UnsupportedOperationException()
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = throw UnsupportedOperationException()
}
