package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 规则的统一抽象。
 *
 * 表示一段来自 CWT 规则文件的规则条目，与对应的 PSI 元素建议智能指针绑定，便于跨线程/缓存安全使用。
 * 所有具体形态（如 [CwtPropertyConfig]、[CwtValueConfig]、[CwtOptionConfig] 等）都实现自该接口。
 * 另外还实现了 [UserDataHolder]，以便保存额外的用户数据。
 *
 * 参考：
 * - CWTools 指引：[references/cwt/guidance.md](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)
 * - PLS 规则系统说明：[config.md](https://windea.icu/Paradox-Language-Support/config.md)
 *
 * @property pointer 指向对应的 PSI 元素的智能指针。
 * @property configGroup 所属规则分组（按项目与游戏类型划分）。
 * @property configExpression 绑定到该规则的数据表达式，部分场景可能为 null。
 *
 * @see CwtFileConfig
 * @see CwtMemberConfig
 * @see CwtOptionMemberConfig
 * @see CwtDelegatedConfig
 * @see CwtDetachedConfig
 */
interface CwtConfig<out T : PsiElement> : UserDataHolder {
    val pointer: SmartPsiElementPointer<out T>
    val configGroup: CwtConfigGroup
    val configExpression: CwtDataExpression? get() = null
}
