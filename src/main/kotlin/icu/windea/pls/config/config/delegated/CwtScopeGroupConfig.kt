package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtScopeGroupConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * @property name (key) string
 * @property values (value) string[]
 */
interface CwtScopeGroupConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty(": string[]")
    val values: Set<@CaseInsensitive String>

    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtScopeGroupConfig?
    }

    companion object : Resolver by CwtScopeGroupConfigResolverImpl()
}
