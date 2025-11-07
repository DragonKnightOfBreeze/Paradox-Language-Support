package icu.windea.pls.config.config

import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.impl.CwtValueConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.model.CwtType

/**
 * 值规则（值型成员规则）。
 *
 * 对应 CWT 规则文件中的一个值（`v`）。可以是属性的值，也可以是单独的值。
 *
 * @property propertyConfig 对应属性的值时，所属的属性规则。
 * @property configExpression 绑定到该规则的数据表达式（等同于 [valueExpression]）。
 *
 * @see CwtValue
 */
interface CwtValueConfig : CwtMemberConfig<CwtValue> {
    val propertyConfig: CwtPropertyConfig?

    override val configExpression: CwtDataExpression get() = valueExpression

    interface Resolver {
        /** 由 [CwtValue] 解析为值规则。 */
        fun resolve(element: CwtValue, file: CwtFile, configGroup: CwtConfigGroup): CwtValueConfig

        /** 通过直接解析（即 [resolve]）的方式创建了规则后，需要进行的后续处理（应用特殊选项、从数据表达式收集信息）。 */
        fun postProcess(config: CwtValueConfig)

        /** 通过直接解析（即 [resolve]）以外的方式创建了规则后，需要进行的后续优化。 */
        fun postOptimize(config: CwtValueConfig)

        fun create(
            pointer: SmartPsiElementPointer<out CwtValue>,
            configGroup: CwtConfigGroup,
            value: String,
            valueType: CwtType = CwtType.String,
            configs: List<CwtMemberConfig<*>>? = null,
            optionConfigs: List<CwtOptionMemberConfig<*>> = emptyList(),
            propertyConfig: CwtPropertyConfig? = null,
        ): CwtValueConfig

        fun copy(
            targetConfig: CwtValueConfig,
            pointer: SmartPsiElementPointer<out CwtValue> = targetConfig.pointer,
            value: String = targetConfig.value,
            valueType: CwtType = targetConfig.valueType,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
            optionConfigs: List<CwtOptionMemberConfig<*>> = targetConfig.optionConfigs,
            propertyConfig: CwtPropertyConfig? = targetConfig.propertyConfig,
        ): CwtValueConfig

        /**
         * 基于属性型成员规则，解析出其值侧对应的值型成员规则。
         */
        fun resolveFromPropertyConfig(
            pointer: SmartPsiElementPointer<out CwtValue>,
            propertyConfig: CwtPropertyConfig
        ): CwtValueConfig

        /**
         * 创建 [targetConfig] 的委托规则，并指定要替换的子规则列表。父规则会被重置为 `null`。
         */
        fun delegated(
            targetConfig: CwtValueConfig,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
        ): CwtValueConfig

        /**
         * 创建 [targetConfig] 的委托规则，并指定要替换的值。父规则会被重置为 `null`。
         */
        fun delegatedWith(
            targetConfig: CwtValueConfig,
            value: String,
        ): CwtValueConfig
    }

    companion object : Resolver by CwtValueConfigResolverImpl()
}
