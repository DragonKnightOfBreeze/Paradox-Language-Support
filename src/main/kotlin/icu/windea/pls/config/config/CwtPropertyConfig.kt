package icu.windea.pls.config.config

import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.impl.CwtPropertyConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType

/**
 * 属性规则（属性型成员规则）。
 *
 * 对应 CWT 规则文件中的一个属性（`k = v` 或 `k = {...}`）。
 *
 * @property key 属性键（去除首尾的双引号）。
 * @property separatorType 分隔符类型。
 * @property value 属性值（去除首尾的双引号）。
 * @property valueType 属性值的类型，用于驱动解析与校验。
 * @property valueConfig 属性值对应的值规则。懒加载，且在属性值无法解析时返回 null。
 * @property keyExpression 属性键对应的数据表达式，用于驱动解析与校验。
 * @property valueExpression 属性值对应的数据表达式，用于驱动解析与校验。
 * @property configExpression 绑定到该规则的数据表达式（等同于 [keyExpression]）。
 *
 * @see CwtProperty
 */
interface CwtPropertyConfig : CwtMemberConfig<CwtProperty> {
    val key: String
    val separatorType: CwtSeparatorType
    override val value: String
    override val valueType: CwtType

    val valueConfig: CwtValueConfig?

    val keyExpression: CwtDataExpression
    override val valueExpression: CwtDataExpression
    override val configExpression: CwtDataExpression

    interface Resolver {
        /** 由 [CwtProperty] 解析为属性规则。 */
        fun resolve(element: CwtProperty, file: CwtFile, configGroup: CwtConfigGroup): CwtPropertyConfig?

        fun withConfigs(config: CwtPropertyConfig, configs: List<CwtMemberConfig<*>>): Boolean

        /** 通过直接解析（即 [resolve]）的方式创建了规则后，需要进行的后续处理。 */
        fun postProcess(config: CwtPropertyConfig)

        /** 通过直接解析（即 [resolve]）以外的方式创建了规则后，需要进行的后续优化。 */
        fun postOptimize(config: CwtPropertyConfig)

        /** 创建属性规则。其中的选项数据仍然需要手动初始化。 */
        fun create(
            pointer: SmartPsiElementPointer<out CwtProperty>,
            configGroup: CwtConfigGroup,
            key: String,
            value: String,
            valueType: CwtType = CwtType.String,
            separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
            configs: List<CwtMemberConfig<*>>? = null,
            injectable: Boolean = false,
        ): CwtPropertyConfig

        /** 创建基于 [targetConfig] 的复制规则。其中的规则数据仍然需要手动合并。 */
        fun copy(
            targetConfig: CwtPropertyConfig,
            pointer: SmartPsiElementPointer<out CwtProperty> = targetConfig.pointer,
            key: String = targetConfig.key,
            value: String = targetConfig.value,
            valueType: CwtType = targetConfig.valueType,
            separatorType: CwtSeparatorType = targetConfig.separatorType,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
        ): CwtPropertyConfig

        /** 创建基于 [targetConfig] 的委托规则，并指定要替换的子规则列表。父规则会被重置为 `null`。 */
        fun delegated(
            targetConfig: CwtPropertyConfig,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
        ): CwtPropertyConfig

        /** 创建基于 [targetConfig] 的委托规则，并指定要替换的键和值。父规则会被重置为 `null`。 */
        fun delegatedWith(
            targetConfig: CwtPropertyConfig,
            key: String,
            value: String,
        ): CwtPropertyConfig
    }

    companion object : Resolver by CwtPropertyConfigResolverImpl()
}
