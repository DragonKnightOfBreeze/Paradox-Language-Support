package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 规则的统一抽象。
 *
 * 概述：
 * - 表示一段来自 `.cwt` 文件的“规则条目”，与其对应的 PSI 元素建立智能指针绑定，便于跨线程/缓存安全使用。
 * - 所有具体形态（如 [CwtPropertyConfig]、[CwtValueConfig]、[CwtOptionConfig] 等）都实现自该接口。
 * - 暴露所属的规则组与可选的规则表达式，用于匹配与校验。
 *
 * 参考：
 * - CWTools 指引：[references/cwt/guidance.md](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)
 * - PLS 规则系统说明：[config.md](https://windea.icu/Paradox-Language-Support/config.md)
 *
 * @property pointer 指向承载该规则的 PSI 元素的智能指针（[SmartPsiElementPointer]）。
 * @property configGroup 所属规则组（按游戏、版本、模块划分）。
 * @property configExpression 绑定到该规则的“规则表达式”，部分场景可能为 null。
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
    val pointer: SmartPsiElementPointer<out T>
    val configGroup: CwtConfigGroup
    val configExpression: CwtDataExpression? get() = null
}
