package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * CWT 规则（扩展规则）的最小抽象。
 *
 * 该接口为规则对象提供统一的访问入口（如 PSI 指针、所属规则组、规则表达式）。
 * 具体成员规则请参见 [CwtMemberConfig] 及其子类型。
 *
 * @property pointer 指向源 PSI 元素的智能指针，用于在 PSI 变化时保持稳定引用。
 * @property configGroup 所属的规则组 [CwtConfigGroup]，用于提供跨文件/跨来源的上下文。
 * @property configExpression 规则对应的数据表达式 [CwtDataExpression]。默认返回 null，由具体实现覆盖；
 * 在成员规则中通常分别对应“键表达式”或“值表达式”。
 */
interface CwtConfig<out T : PsiElement> : UserDataHolder {
    val pointer: SmartPsiElementPointer<out T>
    val configGroup: CwtConfigGroup
    val configExpression: CwtDataExpression? get() = null
}
