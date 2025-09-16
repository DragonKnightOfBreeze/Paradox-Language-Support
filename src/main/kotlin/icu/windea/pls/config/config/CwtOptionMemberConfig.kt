package icu.windea.pls.config.config

import com.intellij.psi.PsiElement
import icu.windea.pls.model.CwtType

/**
 * CWT config 的“选项项”抽象。
 *
 * 概述：
 * - 对应 .cwt 成员条目（[CwtMemberConfig]）下以 `##` 声明的选项（option）。
 * - 分为两类：键值选项（[CwtOptionConfig]）与值选项（[CwtOptionValueConfig]）。
 * - 选项自身也可以携带下级选项（递归结构），以构成更复杂的配置描述。
 *
 * 参考：
 * - references/cwt/guidance.md（选项语义与写法）
 * - docs/zh/config.md（PLS 对选项的扩展与消费场景）
 */
sealed interface CwtOptionMemberConfig<out T : PsiElement> : CwtDetachedConfig {
    /** 选项的原始值文本。 */
    val value: String
    /** 选项值类型，用于后续解析与校验。 */
    val valueType: CwtType
    /** 下级选项集合（如 `## option = { x = 1 y = 2 }`），无则为 null。 */
    val optionConfigs: List<CwtOptionMemberConfig<*>>?
}
