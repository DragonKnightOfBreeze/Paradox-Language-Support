package icu.windea.pls.config.config

import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.model.CwtType

/**
 * 成员规则。
 *
 * 对应 CWT 规则文件中的一个属性（`k = v` 或 `k = { ... }`）或值（`v`）。
 *
 * @property value 值（去除首尾的双引号）。
 * @property valueType 值的类型，用于驱动解析与校验。
 * @property configs 子规则列表（其中嵌套的属性与值对应的成员规则）。
 * @property optionConfigs 附加的选项成员规则列表（来自附加的选项注释，以 `## ...` 的形式声明）。
 * @property parentConfig 父级成员规则（若存在），用于溯源与继承/推断。
 * @property valueExpression 值对应的数据表达式，用于驱动解析与校验。
 * @property configExpression 绑定到该规则的数据表达式（等同于 [CwtPropertyConfig.keyExpression] 或 [CwtValueConfig.valueExpression]）。
 *
 * @see CwtMemberElement
 */
sealed interface CwtMemberConfig<out T : CwtMemberElement> : CwtConfig<T> {
    val value: String
    val valueType: CwtType
    val configs: List<CwtMemberConfig<*>>?
    val optionConfigs: List<CwtOptionMemberConfig<*>>?

    var parentConfig: CwtMemberConfig<*>?

    val valueExpression: CwtDataExpression
    override val configExpression: CwtDataExpression

    override fun toString(): String

    object Keys : KeyRegistry()
}
