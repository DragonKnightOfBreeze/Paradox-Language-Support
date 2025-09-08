package icu.windea.pls.config.config

import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.impl.CwtValueConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.model.CwtType

interface CwtValueConfig : CwtMemberConfig<CwtValue> {
    val propertyConfig: CwtPropertyConfig?

    override val configExpression: CwtDataExpression get() = valueExpression

    interface Resolver {
        fun resolve(
            pointer: SmartPsiElementPointer<out CwtValue>,
            configGroup: CwtConfigGroup,
            value: String,
            valueType: CwtType = CwtType.String,
            configs: List<CwtMemberConfig<*>>? = null,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
            propertyConfig: CwtPropertyConfig? = null
        ): CwtValueConfig

        fun resolveFromPropertyConfig(
            pointer: SmartPsiElementPointer<out CwtValue>,
            propertyConfig: CwtPropertyConfig
        ): CwtValueConfig

        fun delegated(
            targetConfig: CwtValueConfig,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
            parentConfig: CwtMemberConfig<*>? = targetConfig.parentConfig,
        ): CwtValueConfig

        fun delegatedWith(
            targetConfig: CwtValueConfig,
            value: String
        ): CwtValueConfig

        fun copy(
            targetConfig: CwtValueConfig,
            pointer: SmartPsiElementPointer<out CwtValue> = targetConfig.pointer,
            value: String = targetConfig.value,
            valueType: CwtType = targetConfig.valueType,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = targetConfig.optionConfigs,
            propertyConfig: CwtPropertyConfig? = targetConfig.propertyConfig,
        ): CwtValueConfig
    }

    companion object: Resolver by CwtValueConfigResolverImpl()
}
