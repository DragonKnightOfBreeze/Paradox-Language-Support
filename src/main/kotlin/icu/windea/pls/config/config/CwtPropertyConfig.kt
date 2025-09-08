package icu.windea.pls.config.config

import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.impl.CwtPropertyConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType

interface CwtPropertyConfig : CwtMemberConfig<CwtProperty> {
    val key: String
    val separatorType: CwtSeparatorType

    val valueConfig: CwtValueConfig?

    val keyExpression: CwtDataExpression
    override val configExpression: CwtDataExpression get() = keyExpression

    interface Resolver {
        fun resolve(
            pointer: SmartPsiElementPointer<out CwtProperty>,
            configGroup: CwtConfigGroup,
            key: String,
            value: String,
            valueType: CwtType = CwtType.String,
            separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
            configs: List<CwtMemberConfig<*>>? = null,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null
        ): CwtPropertyConfig

        fun delegated(
            targetConfig: CwtPropertyConfig,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
            parentConfig: CwtMemberConfig<*>? = targetConfig.parentConfig
        ): CwtPropertyConfig

        fun delegatedWith(
            targetConfig: CwtPropertyConfig,
            key: String,
            value: String
        ): CwtPropertyConfig

        fun copy(
            targetConfig: CwtPropertyConfig,
            pointer: SmartPsiElementPointer<out CwtProperty> = targetConfig.pointer,
            key: String = targetConfig.key,
            value: String = targetConfig.value,
            valueType: CwtType = targetConfig.valueType,
            separatorType: CwtSeparatorType = targetConfig.separatorType,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = targetConfig.optionConfigs
        ): CwtPropertyConfig
    }

    companion object: Resolver by CwtPropertyConfigResolverImpl()
}
