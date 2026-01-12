package icu.windea.pls.config.config

import icu.windea.pls.config.config.impl.CwtMemberConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.model.CwtType

/**
 * 成员规则。
 *
 * 对应 CWT 规则文件中的一个属性（`k = v` 或 `k = {...}`）或值（`v`）。
 *
 * @property value 值（去除首尾的双引号）。
 * @property valueType 值的类型，用于驱动解析与校验。
 * @property configs 子规则列表（其中嵌套的属性与值对应的成员规则）。
 * @property properties 子属性规则列表（其中嵌套的属性对应的成员规则）。
 * @property values 子值规则列表（其中嵌套的值对应的成员规则）。
 * @property parentConfig 父级成员规则（若存在），用于溯源与继承/推断。
 * @property optionData 选项数据（来自附加的选项注释，以 `## ...` 的形式声明）。
 * @property valueExpression 值对应的数据表达式，用于驱动解析与校验。
 * @property configExpression 绑定到该规则的数据表达式（等同于 [CwtPropertyConfig.keyExpression] 或 [CwtValueConfig.valueExpression]）。
 *
 * @see CwtMember
 */
sealed interface CwtMemberConfig<out T : CwtMember> : CwtMemberContainerConfig<T> {
    val value: String
    val valueType: CwtType
    override val configs: List<CwtMemberConfig<*>>?
    override val properties: List<CwtPropertyConfig>? get() = configs?.filterIsInstance<CwtPropertyConfig>()
    override val values: List<CwtValueConfig>? get() = configs?.filterIsInstance<CwtValueConfig>()
    var parentConfig: CwtMemberConfig<*>?
    val optionData: CwtOptionDataHolder

    val valueExpression: CwtDataExpression
    override val configExpression: CwtDataExpression

    override fun toString(): String

    interface Resolver {
        fun withConfigs(config: CwtMemberConfig<*>, configs: List<CwtMemberConfig<*>>): Boolean

        /** 通过直接解析（即 [resolve]）的方式创建了规则后，需要进行的后续处理。 */
        fun postProcess(config: CwtMemberConfig<*>)

        /** 通过直接解析（即 [resolve]）以外的方式创建了规则后，需要进行的后续优化。 */
        fun postOptimize(config: CwtMemberConfig<*>)

        /** 创建基于 [targetConfig] 的委托规则，并指定要替换的子规则列表。父规则会被重置为 `null`。 */
        fun <T : CwtMemberConfig<*>> delegated(
            targetConfig: T,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
        ): T
    }

    object Keys : KeyRegistry()

    companion object : Resolver by CwtMemberConfigResolverImpl()
}
