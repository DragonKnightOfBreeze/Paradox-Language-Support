package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtTypeImagesConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtTypeImagesConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> // (subtypeExpression, locationConfig)

    /**
     * 得到根据子类型列表进行合并后的配置。
     */
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtTypeImagesConfig?
    }

    companion object : Resolver by CwtTypeImagesConfigResolverImpl()
}
