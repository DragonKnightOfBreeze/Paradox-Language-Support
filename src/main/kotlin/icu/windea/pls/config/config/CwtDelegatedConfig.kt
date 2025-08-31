package icu.windea.pls.config.config

import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * CWT 委托规则。
 *
 * 一类“包装/代理”规则：通过内部的 [config] 转发 [pointer] 与 [configGroup] 等属性，
 * 在不复制底层数据的情况下进行局部调整（如覆盖键/值或追加子成员）。
 *
 * @param E 对应的 PSI 类型。
 * @param C 被委托的规则类型。
 * @property config 被委托的底层规则对象。
 */
interface CwtDelegatedConfig<out E : PsiElement, out C : CwtConfig<E>> : CwtConfig<E> {
    override val pointer: SmartPsiElementPointer<out E> get() = config.pointer
    override val configGroup: CwtConfigGroup get() = config.configGroup

    val config: C
}
