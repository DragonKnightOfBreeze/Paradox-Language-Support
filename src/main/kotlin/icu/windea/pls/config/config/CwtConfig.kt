package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * CWT config 的统一抽象。
 *
 * 概述：
 * - 表示一段来自 `.cwt` 文件的“规则条目”（config），与其对应的 PSI 元素建立弱引用绑定，便于跨线程/缓存安全使用。
 * - 所有具体的 config 形态（如 [CwtPropertyConfig]、[CwtValueConfig]、[CwtOptionConfig] 等）都实现自该接口。
 * - 提供所归属的规则组（[CwtConfigGroup]）与可选的“规则表达式”（[CwtDataExpression]），用于匹配与校验。
 *
 * 参考：
 * - references/cwt/guidance.md（CWTools 指引）
 * - docs/zh/config.md（PLS 规则系统说明）
 *
 * 术语说明：
 * - 本项目中统一将“CWT 规则”翻译为 “CWT config”。
 *
 * @see CwtPropertyConfig
 * @see CwtValueConfig
 * @see CwtOptionConfig
 * @see CwtOptionValueConfig
 * @see CwtMemberConfig
 * @see CwtDelegatedConfig
 * @see CwtDetachedConfig
 */
interface CwtConfig<out T : PsiElement> : UserDataHolder {
    /**
     * 指向承载该 config 的 PSI 元素的智能指针。
     *
     * 说明：
     * - 用智能指针（[SmartPsiElementPointer]）以保证跨线程/缓存场景下的引用安全。
     */
    val pointer: SmartPsiElementPointer<out T>

    /**
     * 该 config 所属的规则组（按游戏、版本、模块划分）。
     */
    val configGroup: CwtConfigGroup

    /**
     * 绑定到该 config 的“规则表达式”。
     *
     * 说明：
     * - 对于属性/值等成员 config，一般会有明确的表达式（见 [CwtPropertyConfig.keyExpression]、[CwtValueConfig.valueExpression]）。
     * - 对于某些顶层或特殊场景，可能不存在表达式，此时为 null。
     */
    val configExpression: CwtDataExpression? get() = null
}
