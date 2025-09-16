package icu.windea.pls.config.config

import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 委托型 CWT config。
 *
 * 概述：
 * - 以“包装（wrapper）/委托（delegation）”的形式复用另一个 config 的语义，并可在必要时只覆盖其中少量字段。
 * - `pointer` 与 `configGroup` 默认直接委托给被包装的 [config]。
 *
 * 典型场景：
 * - 针对同一 PSI 元素生成多个“变体”config（如临时替换 `key`/`value`/`expression`）。
 * - 在保持来源一致的前提下，对 config 进行轻量改写与传递。
 *
 * 参考：
 * - docs/zh/config.md（委托与复制的使用场景）
 *
 * @see CwtPropertyConfig
 * @see CwtValueConfig
 */
interface CwtDelegatedConfig<out E : PsiElement, out C : CwtConfig<E>> : CwtConfig<E> {
    override val pointer: SmartPsiElementPointer<out E> get() = config.pointer
    override val configGroup: CwtConfigGroup get() = config.configGroup

    /**
     * 被委托/包装的原始 config。
     */
    val config: C
}
