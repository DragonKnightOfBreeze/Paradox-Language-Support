package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtDatabaseObjectTypeConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtDatabaseObjectTypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty("type: string?")
    val type: String?
    @FromProperty("swap_type: string?")
    val swapType: String?
    @FromProperty("localisation: string?")
    val localisation: String?

    fun getConfigForType(isBase: Boolean): CwtValueConfig?

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtDatabaseObjectTypeConfig?
    }

    companion object : Resolver by CwtDatabaseObjectTypeConfigResolverImpl()
}
