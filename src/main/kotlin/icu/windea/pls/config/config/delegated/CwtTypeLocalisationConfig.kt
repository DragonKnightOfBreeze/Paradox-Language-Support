@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.impl.CwtTypeLocalisationConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtTypeLocalisationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> // (subtypeExpression, locationConfig)

    /**
     * 得到根据子类型列表进行合并后的配置。
     */
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtTypeLocalisationConfig?
    }

    companion object : Resolver by CwtTypeLocalisationConfigResolverImpl()
}
