package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtEnumConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtEnumConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("enum[$]")
    val name: String
    @FromProperty("values: template_expression[]")
    val values: Set<@CaseInsensitive String>

    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtEnumConfig?
    }

    companion object : Resolver by CwtEnumConfigResolverImpl()
}
