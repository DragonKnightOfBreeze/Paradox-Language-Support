package icu.windea.pls.config.config

import com.intellij.psi.PsiElement

/**
 * 可以获取一组子规则的规则。
 *
 * @property configs 子规则列表（其中的属性与值对应的成员规则）。
 * @property properties 子属性规则列表（其中的属性对应的成员规则）。
 * @property values 子值规则列表（其中的值对应的成员规则）。
 *
 * @see CwtFileConfig
 * @see CwtMemberConfig
 */
interface CwtMemberContainerConfig<out T : PsiElement> : CwtConfig<T> {
    val configs: List<CwtMemberConfig<*>>? get() = null
    val properties: List<CwtPropertyConfig>? get() = null
    val values: List<CwtValueConfig>? get() = null
}
